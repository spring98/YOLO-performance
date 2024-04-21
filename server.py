from flask import Flask, request, jsonify
import datetime

app = Flask(__name__)

@app.route('/submit', methods=['POST'])
def submit():
    data = request.get_json()  # Get JSON data from the request

    if not data or 'temperature_data' not in data:
        return jsonify({'error': 'Missing data'}), 400

    temperature_data = data['temperature_data']  # Expect this to be a list of dictionaries

    if not isinstance(temperature_data, list) or not all('time' in entry and 'temperature' in entry for entry in temperature_data):
        return jsonify({'error': 'Invalid data format'}), 400

    # Save to file
    with open('YOLOv5_320_pytorch.txt', 'a') as file:
        for entry in temperature_data:
            file.write(f"{entry['time']}, {entry['temperature']}, {entry['frame']}\n")

    return jsonify({'message': 'Data saved successfully'}), 200

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=57777)
