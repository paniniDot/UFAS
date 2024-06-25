function createJson(obj, value) {
  const json = {
    name: obj,
    measure: value,
  }
  return JSON.stringify(json);
}
const lightbulbIcon = document.getElementById("icon");
const lightswitch = document.getElementById("lightswitch");
const range = document.getElementById('rollrange');
const valueSpan = document.getElementById('rollvalue');
lightswitch.disabled = false;
range.disabled = false;

document.addEventListener('DOMContentLoaded', () => {
window.updateDashboard = function(name, value) {
  if (name === "light") {
      lightswitch.checked = value;
      if (lightswitch.checked) {
          lightbulbIcon.classList.replace("bi-lightbulb-off", "bi-lightbulb");
      } else {
          lightbulbIcon.classList.replace("bi-lightbulb", "bi-lightbulb-off");
      }
      activateCard("light-card");
  } else if (name === "roll") {
      valueSpan.textContent = `${value}`;
      const offset = ((value - range.min + 2) / (range.max - range.min + 4)) * range.offsetWidth;
      valueSpan.style.transform = `translateX(${offset}px) translateY(-120%)`;
      range.value = value;
      activateCard("roll-card");
  } else if (name === "camera") {
      document.getElementById("image").src = value;
      activateCard("cam-card");
  }
}

function activateCard(cardId) {
  const card = document.getElementById(cardId);
  if (card) {
      card.classList.remove("d-none"); // Show the card
  }
}
});


document.addEventListener('DOMContentLoaded', () => {
  lightswitch.addEventListener("click", () => {
    sendMessage(createJson("light", lightswitch.checked ? 1 : 0));
    if (lightswitch.checked) {
      lightbulbIcon.classList.replace("bi-lightbulb-off", "bi-lightbulb");
    } else {
      lightbulbIcon.classList.replace("bi-lightbulb", "bi-lightbulb-off");
    }
  });
  range.addEventListener('input', (event) => {
    const value = event.target.value;
    sendMessage(createJson("roll", value));
    valueSpan.textContent = `${value}`;
    const offset = ((value - range.min + 2) / (range.max - range.min + 4)) * range.offsetWidth;
    valueSpan.style.transform = `translateX(${offset}px) translateY(-120%)`;
  });
});