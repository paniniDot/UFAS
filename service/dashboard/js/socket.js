const webSocket = new WebSocket('ws://192.168.1.47:8080/ws');

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

