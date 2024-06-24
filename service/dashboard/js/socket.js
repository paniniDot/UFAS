const webSocket = new WebSocket('ws://192.168.1.47:8080/ws');

webSocket.onopen = function () {
  console.log('Socket attivo.');
};

webSocket.onerror = function (error) {
  console.error('Errore nella connessione WebSocket:', error);
};

function sendMessage(message) {
  webSocket.send(message);
  console.log('Messaggio inviato:', message);
}

