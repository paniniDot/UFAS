import { save_data } from './firebase.js';
import { updateChart, updateDashboard } from './dashboard.js';
import { createRoomCard, removeRoom, loadRooms, saveRoom } from './room_utils.js';
const webSocket = new WebSocket('ws://192.168.1.156:8080/ws');

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
    if ( data.room == new URLSearchParams(window.location.search).get('room')) {
      updateDashboard(data.name, data.measure);
      if (data.name == "light" || data.name == "roll") {
        updateChart(data.name, data.timestamp, data.measure);
        //save_data(data)
      }
    }
  }

};

function sendMessage(message) {
  webSocket.send(message);
  console.log('Messaggio inviato:', message);
}


export { sendMessage };