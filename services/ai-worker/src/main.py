import os
import logging
import ollama
from dotenv import load_dotenv
from confluent_kafka import Consumer, Producer, KafkaError # <--- Added Producer

# 1. Setup Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AI-Worker")

# 2. Load Config
load_dotenv()
KAFKA_BROKER = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:19092")
TOPIC_IN = os.getenv("KAFKA_TOPIC_IN", "queries.in")
TOPIC_OUT = os.getenv("KAFKA_TOPIC_OUT", "answers.out") # <--- Load Output Topic
GROUP_ID = os.getenv("KAFKA_GROUP_ID", "ai-worker-group")
AI_MODEL = "llama3"

# 3. Configure Producer
producer_conf = {'bootstrap.servers': KAFKA_BROKER}
producer = Producer(producer_conf)

def delivery_report(err, msg):
    """ Called once for each message produced to indicate delivery result. """
    if err is not None:
        logger.error(f'❌ Message delivery failed: {err}')
    else:
        logger.info(f'🚀 Answer delivered to {msg.topic()} [{msg.partition()}]')

def generate_ai_response(user_query):
    try:
        logger.info(f"🧠 Thinking... Query: {user_query}")
        response = ollama.chat(model=AI_MODEL, messages=[
            {'role': 'system', 'content': 'You are a helpful assistant. Keep answers concise (under 160 chars).'},
            {'role': 'user', 'content': user_query},
        ])
        return response['message']['content']
    except Exception as e:
        logger.error(f"AI Generation Failed: {e}")
        return "Error: Could not generate response."

def start_consumer():
    conf = {
        'bootstrap.servers': KAFKA_BROKER,
        'group.id': GROUP_ID,
        'auto.offset.reset': 'earliest'
    }
    consumer = Consumer(conf)
    consumer.subscribe([TOPIC_IN])
    logger.info(f"🎧 AI Worker listening on {TOPIC_IN}...")

    try:
        while True:
            msg = consumer.poll(1.0)
            if msg is None: continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF: continue
                else:
                    logger.error(f"Consumer error: {msg.error()}")
                    continue

            raw_val = msg.value().decode('utf-8')
            try:
                # 1. Parse Input
                msg_id, user_text = raw_val.split(":", 1)
                logger.info(f"📨 Processing Message ID: {msg_id}")
                
                # 2. Generate AI Answer
                ai_answer = generate_ai_response(user_text)
                
                # 3. Publish to Kafka (Output)
                # Format -> "ID:ANSWER"
                output_payload = f"{msg_id}:{ai_answer}"
                producer.produce(TOPIC_OUT, output_payload.encode('utf-8'), callback=delivery_report)
                producer.flush() # Ensure it's sent immediately

            except ValueError:
                logger.error(f"Invalid message format: {raw_val}")

    except KeyboardInterrupt:
        logger.info("Stopping worker...")
    finally:
        consumer.close()

if __name__ == "__main__":
    start_consumer()