function createRoomCard(roomName) {
    const newCard = document.createElement('div');
    newCard.id = roomName;
    newCard.className = 'card m-2';
    newCard.innerHTML = `
        <div class="card-header">${roomName}</div>
        <div class="card-body">
            <svg xmlns="http://www.w3.org/2000/svg" width="100%" height="150" fill="currentColor"
                 class="bi bi-house" viewBox="0 0 16 16">
                <path
                    d="M8.707 1.5a1 1 0 0 0-1.414 0L.646 8.146a.5.5 0 0 0 .708.708L2 8.207V13.5A1.5 1.5 0 0 0 3.5 15h9a1.5 1.5 0 0 0 1.5-1.5V8.207l.646.647a.5.5 0 0 0 .708-.708L13 5.793V2.5a.5.5 0 0 0-.5-.5h-1a.5.5 0 0 0-.5.5v1.293zM13 7.207V13.5a.5.5 0 0 1-.5.5h-9a.5.5 0 0 1-.5-.5V7.207l5-5z" />
            </svg>
        </div>
        <div class="card-footer">
            <div class="btn-group">
                <a href="room.html?room=${roomName}" class="btn btn-sm btn-outline-secondary">View</a>
                <button type="button" class="btn btn-sm btn-outline-secondary">Edit</button>
                <button type="button" class="btn btn-sm btn-outline-secondary btn-remove">Remove</button>
            </div>
        </div>
    `;
    newCard.querySelector('.btn-remove').addEventListener('click', function () {
        removeRoom(roomName, newCard);
    });
    return newCard;
}

function removeRoom(roomName, cardElement) {
    cardElement.remove();
    const rooms = JSON.parse(localStorage.getItem('rooms')) || [];
    const filteredRooms = rooms.filter(room => room.name !== roomName);
    localStorage.setItem('rooms', JSON.stringify(filteredRooms));
}

function loadRooms() {
    const rooms = JSON.parse(localStorage.getItem('rooms')) || [];
    const container = document.getElementById('cardContainer');
    rooms.forEach(room => {
        container.appendChild(createRoomCard(room.name));
    });
}

function saveRoom(name) {
    const rooms = JSON.parse(localStorage.getItem('rooms')) || [];
    rooms.push({ name: name });
    localStorage.setItem('rooms', JSON.stringify(rooms));
}

if (window.location.pathname.includes('house.html')) {
    window.onload = loadRooms;
}

export { createRoomCard, removeRoom, loadRooms, saveRoom };