// firebase.js

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { getDatabase, ref, set, push, onValue, query, orderByChild, limitToLast } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";

const firebaseConfig = {
    apiKey: "AIzaSyByGTsHmdsNrkeDsleCxT6MrjFFZHCHE9g",
    authDomain: "ufas-f1ebf.firebaseapp.com",
    databaseURL: "https://ufas-f1ebf-default-rtdb.europe-west1.firebasedatabase.app",
    projectId: "ufas-f1ebf",
    storageBucket: "ufas-f1ebf.appspot.com",
    messagingSenderId: "510856631971",
    appId: "1:510856631971:web:e68430d929a227fe0a9116",
    measurementId: "G-HP9QGLMVN9"
};

const app = initializeApp(firebaseConfig);
const database = getDatabase(app);

function save_data(data) {
    const dataRef = ref(database, `rooms/${data.room}/${data.name}`);
    const newDataRef = push(dataRef);
    set(newDataRef, {
        measure: data.measure,
        timestamp: data.timestamp
    }).then(() => {
        console.log('Data saved successfully');
    }).catch((error) => {
        console.error('Error saving data:', error);
    });
}

function load_data(name, limit) {
    const room = new URLSearchParams(window.location.search).get('room');
    const dataRef = query(ref(database, `rooms/${room}/${name}`), orderByChild('timestamp'), limitToLast(limit));
    const data = [];
    onValue(dataRef, (snapshot) => {
        snapshot.forEach((childSnapshot) => {
            data.push(childSnapshot.val());
        });
    }, (error) => {
        console.error('Error loading recent data:', error);
    });
    return data;
}

export { save_data, load_data };
