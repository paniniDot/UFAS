// firebase.js

import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { getDatabase, ref, set,child,get, push, onValue, query, orderByChild, limitToLast } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";

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

async function load_data(name) {
    const room = new URLSearchParams(window.location.search).get('room');
    const dataRef = ref(database);

    try {
        const snapshot = await get(child(dataRef, `rooms/${room}/${name}`));
        if (snapshot.exists()) {
            console.log('Data loaded successfully');
            let data = snapshot.val();
            return data;
        } else {
            console.error('No data found');
            return null; // Return null if no data found
        }
    } catch (error) {
        console.error('Error loading data:', error);
        return null; // Return null in case of error
    }
}

export { save_data, load_data };
