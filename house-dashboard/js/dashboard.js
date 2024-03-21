function createJson(obj, value) {
  const json = {
    name: obj,
    measure: value,
  }
  return JSON.stringify(json);
}
const rollButton = document.getElementById("rollcheck");
const lightswitch = document.getElementById("lightswitch");
const lightButton = document.getElementById("lightcheck");
const lightbulbIcon = document.getElementById("icon");
const range = document.getElementById('rollrange');
const valueSpan = document.getElementById('rollvalue');
lightswitch.disabled = true;
range.disabled = true;

function updateDashboard(name, value) {
  if (name == "light") {
    lightswitch.checked = value > 0;
    if (lightswitch.checked) {
      lightbulbIcon.classList.replace("bi-lightbulb-off", "bi-lightbulb");
    } else {
      lightbulbIcon.classList.replace("bi-lightbulb", "bi-lightbulb-off");
    }
  } else if (name == "roll") {
    valueSpan.textContent = `${value}`;
    const offset = ((value - range.min + 2) / (range.max - range.min + 4)) * range.offsetWidth;
    valueSpan.style.transform = `translateX(${offset}px) translateY(-120%)`;
    range.value = value;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  lightButton.addEventListener("click", () => {
    if (lightButton.checked) {
      lightswitch.disabled = false;
    } else {
      lightswitch.disabled = true;
    }
    lightswitch.addEventListener("click", () => {
      sendMessage(createJson("light", lightswitch.checked ? 1 : 0));
      if (lightswitch.checked) {
        lightbulbIcon.classList.replace("bi-lightbulb-off", "bi-lightbulb");
      } else {
        lightbulbIcon.classList.replace("bi-lightbulb", "bi-lightbulb-off");
      }
    });
    sendMessage(createJson("manual_light", lightButton.checked ? 1 : 0));
  });

  rollButton.addEventListener("click", () => {
    if (rollButton.checked) {
      range.disabled = false;
    } else {
      range.disabled = true;
    }
    range.addEventListener('input', (event) => {
      const value = event.target.value;
      sendMessage(createJson("roll", value));
      valueSpan.textContent = `${value}`;
      const offset = ((value - range.min + 2) / (range.max - range.min + 4)) * range.offsetWidth;
      valueSpan.style.transform = `translateX(${offset}px) translateY(-120%)`;
    });
    sendMessage(createJson("manual_roll", rollButton.checked ? 1 : 0));
  });
});