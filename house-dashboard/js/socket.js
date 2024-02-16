const webSocket = new WebSocket('ws://localhost:8080');

webSocket.onopen = function () {
  console.log('Socket attivo.');
};

webSocket.onerror = function (error) {
  console.error('Errore nella connessione WebSocket:', error);
};

webSocket.onmessage = function receiveMessage(event) {
  const data = JSON.parse(event.data);
  console.log(data);
  const measure = data.measure;
  const name = data.name;
  const namechart = name + "chart";
  updateChart(namechart, chartData[name].data, chartData[name].layout, data.timestamp, data.measure);
  updateDashboard(name, measure);
}

function sendMessage(message) {
  webSocket.send(message);
  console.log('Messaggio inviato:', message);
}
