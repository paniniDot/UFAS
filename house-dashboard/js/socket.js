const webSocket = new WebSocket('ws://localhost:8080/eventbus');

webSocket.onopen = function () {
  console.log('Socket attivo.');
};

webSocket.onerror = function (error) {
  console.error('Errore nella connessione WebSocket:', error);
};

webSocket.onmessage = function receiveMessage(event) {
  console.log('WebSocket message received:', event);
}

function sendMessage(message) {
  webSocket.send(message);
  console.log('Messaggio inviato:', message);
}
