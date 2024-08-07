import { load_data } from './firebase.js';
import { sendMessage } from './socket.js';

const room = new URLSearchParams(window.location.search).get('room')

const chartData = {
  roll: {
    data: [
      {
        x: [],
        y: [],
        type: 'scatter',
        mode: 'lines',
      }
    ],
    layout: {
      title: 'Storico tenda',
      xaxis: {
        title: 'hh:mm:ss',
        type: 'date',
      },
      yaxis: {
        title: 'aperto in %',
        range: [0, 100]
      }
    },
  },
  light: {
    data: [
      {
        x: [],
        y: [],
        type: 'scatter',
        mode: 'lines',
      }
    ],
    layout: {
      title: 'Storico luce',
      xaxis: {
        title: 'hh:mm:ss',
        type: 'date',
      },
      yaxis: {
        title: 'acceso in %',
        range: [0, 1]
      }
    },
  }
};

function createJson(obj, value) {
  const json = {
    name: obj,
    room: room,
    measure: value,
  }
  return JSON.stringify(json);
}

function initializeChart(chartId, data, layout) {
  Plotly.newPlot(chartId, data, layout, {
    displayModeBar: true,
    responsive: true
  });
}

function loadChart(name, data) {
  // Convert object to array if data is an object
  if (!Array.isArray(data)) {
    data = Object.values(data);
  }
  //data = data.slice(-4);
  data.forEach((item) => {
    const date = new Date(item.timestamp * 1000);
    chartData[name].data[0].x.push(date);
    chartData[name].data[0].y.push(item.measure);
  });

  Plotly.update(name + "chart", chartData[name].data, chartData[name].layout);
}

function updateChart(name, time, value) {
  const date = new Date(time * 1000);

  if (chartData[name].data[0].x.length >= 4) {
    //chartData[name].data[0].x.shift();
    //chartData[name].data[0].y.shift();
  }

  chartData[name].data[0].x.push(date);
  chartData[name].data[0].y.push(value);

  Plotly.update(name + "chart", chartData[name].data, chartData[name].layout);
}


function createRollCard() {
  return `
      <div id="roll-card" class="card m-2" style="min-width: 600px;">
          <div class="card-header">roll</div>
          <div class="card-body p-0">
              <form class="col-12">
                  <div class="form-check">
                      <input class="form-check-input" type="checkbox" value="" id="rollcheck">
                      <label class="form-check-label" for="rollcheck">manuale</label>
                  </div>
                  <label for="rollrange" class="form-label"></label>
                  <div class="d-flex justify-content-around">
                      <i class="bi bi-arrow-down"></i>
                      <input type="range" class="form-range" min="0" max="100" step="10" value="0" id="rollrange">
                      <span class="position-absolute badge start-0 rounded-pill bg-primary"
                          style="transform: translateX(+12px) translateY(-120%);" id="rollvalue">0
                      </span>
                      <i class="bi bi-arrow-up"></i>
                  </div>
              </form>
          </div>
          <div class="card-footer">
              <div id="rollchart"></div>
          </div>
      </div>
  `;
}

function createLightCard() {
  return `
      <div id="light-card" class="card m-2" style="min-width: 600px;">
          <div class="card-header">light</div>
          <div class="card-body">
              <form>
                  <div class="form-check">
                      <input class="form-check-input" type="checkbox" value="" id="lightcheck">
                      <label class="form-check-label" for="lightcheck">manuale</label>
                  </div>
                  <div class="form-check form-switch">
                      <input class="form-check-input" type="checkbox" role="switch" id="lightswitch">
                      <label class="form-check-label" for="lightswitch">light</label>
                      <i class="bi bi-lightbulb-off" id="icon"></i>
                  </div>
              </form>
          </div>
          <div class="card-footer">
              <div id="lightchart"></div>
          </div>
      </div>
  `;
}

function createCamCard() {
  return `
      <div id="cam-card" class="card m-2" style="min-width: 600px;">
          <div class="card-header">camera</div>
          <div class="card-body">
              <img id="image" src="" alt="Live Webcam">
          </div>
          <div class="card-footer">
              <div id="lightchart"></div>
          </div>
      </div>
  `;
}

async function updateDashboard(name, value) {
  const container = document.getElementById('cardContainer');

  if (name === "light" && !document.getElementById("light-card")) {
    container.insertAdjacentHTML('beforeend', createLightCard());
    initializeChart('lightchart', chartData.light.data, chartData.light.layout);
    let data = await load_data("light");
    loadChart("light", data);
  } else if (name === "roll" && !document.getElementById("roll-card")) {
    container.insertAdjacentHTML('beforeend', createRollCard());
    initializeChart('rollchart', chartData.roll.data, chartData.roll.layout);
    let data = await load_data("roll");
    loadChart("roll", data);
  } else if (name === "camera" && !document.getElementById("cam-card")) {
    container.insertAdjacentHTML('beforeend', createCamCard());
  }

  if (name === "light") {
    const lightbulbIcon = document.getElementById("icon");
    const lightswitch = document.getElementById("lightswitch");
    lightswitch.disabled = false;
    lightswitch.checked = value;
    if (lightswitch.checked) {
      lightbulbIcon.classList.replace("bi-lightbulb-off", "bi-lightbulb");
    } else {
      lightbulbIcon.classList.replace("bi-lightbulb", "bi-lightbulb-off");
    }
  } else if (name === "roll") {
    const range = document.getElementById('rollrange');
    const valueSpan = document.getElementById('rollvalue');
    range.disabled = false;
    valueSpan.textContent = `${value}`;
    const offset = ((value - range.min + 2) / (range.max - range.min + 4)) * range.offsetWidth;
    valueSpan.style.transform = `translateX(${offset}px) translateY(-120%)`;
    range.value = value;
  } else if (name === "camera") {
    document.getElementById("image").src = value;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('cardContainer').addEventListener('click', (event) => {
    if (event.target.id === "lightswitch") {
      sendMessage(createJson("light", event.target.checked ? 1 : 0));
      const lightbulbIcon = document.getElementById("icon");
      if (event.target.checked) {
        lightbulbIcon.classList.replace("bi-lightbulb-off", "bi-lightbulb");
      } else {
        lightbulbIcon.classList.replace("bi-lightbulb", "bi-lightbulb-off");
      }
    }
  });

  document.getElementById('cardContainer').addEventListener('change', (event) => {
    if (event.target.id === 'rollrange') {
      const value = event.target.value;
      sendMessage(createJson("roll", value));
      const valueSpan = document.getElementById('rollvalue');
      valueSpan.textContent = `${value}`;
      const offset = ((value - event.target.min + 2) / (event.target.max - event.target.min + 4)) * event.target.offsetWidth;
      valueSpan.style.transform = `translateX(${offset}px) translateY(-120%)`;
    }
  });

  document.getElementById('cardContainer').addEventListener('input', (event) => {
    if (event.target.id === 'rollcheck') {
      const range = document.getElementById('rollrange');
      if (event.target.checked) {
        range.disabled = false;
        sendMessage(createJson("manual_roll", 1));
      } else {
        range.disabled = true;
        sendMessage(createJson("manual_roll", 0));
      }
    }
  });

  document.getElementById('cardContainer').addEventListener('input', (event) => {
    if (event.target.id === 'lightcheck') {

      const lightswitch = document.getElementById("lightswitch");
      if (event.target.checked) {
        lightswitch.disabled = false;
        sendMessage(createJson("manual_light", 1));
      } else {
        lightswitch.disabled = true;
        sendMessage(createJson("manual_light", 0));
      }
    }
  });
});


export { updateChart, updateDashboard }

