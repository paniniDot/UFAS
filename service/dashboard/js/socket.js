const webSocket = new WebSocket('ws://192.168.2.226:8080/ws');

webSocket.onopen = function () {
  console.log('Socket attivo.');
};

webSocket.onerror = function (error) {
  console.error('Errore nella connessione WebSocket:', error);
};

webSocket.onmessage = (event) => {
  const data = JSON.parse(event.data);
  const { src, measure, timestamp, room } = data;

  if (window.location.pathname.includes('house.html')) {
    let roomDiv = document.getElementById(room);
    if (!roomDiv) {
      const container = document.getElementById('cardContainer');
      const newCard = createRoomCard(room);
      container.appendChild(newCard);
      saveRoom(room);
    }
  }
  if (window.location.pathname.includes('room.html')) {
    console.log('WebSocket message received:', event);
    const data = JSON.parse(event.data);
    const measure = data.measure;
    const name = data.name;
    const room = data.room;
    const namechart = name + "chart";
    if (room == new URLSearchParams(window.location.search).get('room')) {
      updateDashboard(name, measure);
      if (name == "light" || name == "roll") {
        updateChart(namechart, chartData[name].data, chartData[name].layout, data.timestamp, data.measure);
      }
    }
  }

};

function sendMessage(message) {
  webSocket.send(message);
  console.log('Messaggio inviato:', message);
}

