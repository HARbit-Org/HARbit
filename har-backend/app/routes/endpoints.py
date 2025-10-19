from flask import Blueprint, request, jsonify
from marshmallow import ValidationError
from loguru import logger
from models.request_models import DataRequestSchema
from models.response_models import DataResponseSchema
from services.data_processor import process_data

bp = Blueprint('api', __name__, url_prefix='/api')

@bp.route('/classify', methods=['POST'])
def process_data_endpoint():
    try:
        # Validar datos de entrada
        schema = DataRequestSchema()
        try:
            data_request = schema.load(request.json)
        except ValidationError as err:
            logger.error(f"Error de validación: {err.messages}")
            return jsonify({'error': 'Datos inválidos', 'details': err.messages}), 422
        
        # logger.info(f"Datos recibidos: {data_request}")
        
        # Access to main data
        data_request = data_request["batches"]

        batches_joined = []
        number_of_batches = 0
        principal_timestamp = None
        for id in range(len(data_request)):
            number_of_batches += 1
            batches_joined.extend(data_request[id]['readings'])
            if id == 0:
                principal_timestamp = data_request[id]['timestamp']

        logger.info(f"Target timestamp: {principal_timestamp}")
        logger.info(f"Total readings to process: {number_of_batches} batches")
        processed_data = process_data(batches_joined, principal_timestamp)
        
        # Preparar respuesta
        response_schema = DataResponseSchema()
        response_data = response_schema.dump({'data': processed_data})
        
        return jsonify(response_data), 200
        
    except Exception as e:
        logger.error(f"Error procesando datos: {str(e)}")
        return jsonify({'error': 'Error interno del servidor', 'details': str(e)}), 500

@bp.route('/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'healthy', 'service': 'flask-har-processor'}), 200