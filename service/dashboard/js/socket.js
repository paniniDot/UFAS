const webSocket = new WebSocket('ws://localhost:8080/ws');

webSocket.onopen = function () {
  console.log('Socket attivo.');
};

webSocket.onerror = function (error) {
  console.error('Errore nella connessione WebSocket:', error);
};

webSocket.onmessage = function receiveMessage(event) {
  console.log('WebSocket message received:', event);
  const data = JSON.parse(event.data);
  const measure = data.measure;
  const name = data.name;
  const namechart = name + "chart";
  if (name == "light" || name == "roll") {
    updateChart(namechart, chartData[name].data, chartData[name].layout, data.timestamp, data.measure);
  }
  updateDashboard(name, measure);
}

function sendMessage(message) {
  webSocket.send(message);
  console.log('Messaggio inviato:', message);
}


document.getElementById('websocketForm').addEventListener('submit', function (event) {
  event.preventDefault();
  const messageInput = document.getElementById('message');
  const message = messageInput.value;
  sendMessage(message);
  messageInput.value = ''; 
});
