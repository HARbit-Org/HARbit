from flask import Flask
from loguru import logger
from routes.endpoints import bp
import signal
import sys
import os

# Configurar variables de entorno antes de importar las librerías
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'
os.environ['OPENBLAS_NUM_THREADS'] = '1'
os.environ['MKL_NUM_THREADS'] = '1'

def signal_handler(sig, frame):
    logger.info('Aplicación interrumpida por el usuario')
    sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

def create_app():
    app = Flask(__name__)
    
    # Configuración
    app.config['JSON_SORT_KEYS'] = False
    
    # Registrar blueprints
    app.register_blueprint(bp)
    
    logger.info("Flask HAR Processor iniciado")
    
    return app

app = create_app()

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)