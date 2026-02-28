import os
import json
import logging
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

def start_consumer():
    # 3. Configure Kafka Consumer
    conf = {
        'bootstrap.servers': KAFKA_BROKER,
        'group.id': GROUP_ID,
        'auto.offset.reset': 'earliest' # Read from beginning if new
    }

    consumer = Consumer(conf)
    consumer.subscribe([TOPIC_IN])
    
    logger.info(f"🎧 AI Worker listening on {TOPIC_IN} via {KAFKA_BROKER}...")

    try:
        while True:
            # 4. Poll for messages (1.0 second timeout)
            msg = consumer.poll(1.0)

            if msg is None:
                continue
            if msg.error():
                if msg.error().code() == KafkaError._PARTITION_EOF:
                    continue
                else:
                    logger.error(f"Consumer error: {msg.error()}")
                    continue

            # 5. Process Message
            raw_val = msg.value().decode('utf-8')
            key = msg.key().decode('utf-8') if msg.key() else "NoKey"
            
            logger.info(f"✅ Received Event! Key: {key} | Payload: {raw_val}")
            
            # TODO: Phase 2.5 - Send this payload to Ollama/AI

    except KeyboardInterrupt:
        logger.info("Stopping worker...")
    finally:
        consumer.close()

if __name__ == "__main__":
    start_consumer()