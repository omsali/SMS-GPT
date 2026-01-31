import os
import logging
import ollama  # <--- NEW IMPORT
from dotenv import load_dotenv
from confluent_kafka import Consumer, KafkaError

# 1. Setup Logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AI-Worker")

# 2. Load Config
load_dotenv()
KAFKA_BROKER = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "localhost:19092")
TOPIC_IN = os.getenv("KAFKA_TOPIC_IN", "queries.in")
GROUP_ID = os.getenv("KAFKA_GROUP_ID", "ai-worker-group")
AI_MODEL = "llama3" # <--- Or "tinyllama" if you downloaded that

def generate_ai_response(user_query):
    """
    Calls the local Ollama instance to generate a response.
    """
    try:
        logger.info(f"🧠 Thinking... Query: {user_query}")
        
        # Call Ollama API
        response = ollama.chat(model=AI_MODEL, messages=[
            {
                'role': 'system',
                'content': 'You are a helpful assistant for rural farmers. Keep answers concise (under 160 chars if possible).'
            },
            {
                'role': 'user',
                'content': user_query
            },
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

            # Process Message
            # Payload format from Java is "ID:MESSAGE"
            raw_val = msg.value().decode('utf-8')
            
            try:
                # Split ID and Text
                msg_id, user_text = raw_val.split(":", 1)
                
                logger.info(f"📨 Processing Message ID: {msg_id}")
                
                # --- THE AI STEP ---
                ai_answer = generate_ai_response(user_text)
                
                logger.info(f"🤖 AI Answer: {ai_answer}")
                
                # TODO: Phase 3 - Produce this answer to 'answers.out' Kafka topic
                
            except ValueError:
                logger.error(f"Invalid message format: {raw_val}")

    except KeyboardInterrupt:
        logger.info("Stopping worker...")
    finally:
        consumer.close()

if __name__ == "__main__":
    start_consumer()