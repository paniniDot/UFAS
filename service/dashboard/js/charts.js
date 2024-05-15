const chartData = {
  roll: {
    data: [
      {
        x: [],
        y: [],
        type: 'bar',
      }
    ],
    layout: {
      title: 'Storico tenda',
      xaxis: {
        title: 'giorno-ora',
        type: 'category',
        range: [0, 4],
        tickformat: '%D-%H'
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
        type: 'bar',
      }
    ],
    layout: {
      title: 'Storico luce',
      xaxis: {
        title: 'giorno-ora',
        range: [0, 4],
        type: 'category',
        tickformat: '%D-%H'
      },
      yaxis: {
        title: 'acceso in %',
        range: [0, 100]
      }
    },
  }
};

function initializeChart(chartId, data, layout) {
  Plotly.newPlot(chartId, data, layout, { displayModeBar: true,
    responsive: true });
}

function updateChart(chartId, data, layout, time, value) {
  const date = new Date(time);
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');

  if (data[0].x.length >= 4) {
    data[0].x.shift();
    data[0].y.shift();
  }

  if (value > 0) {
    const Column = `${day}-${hours}`;
    if (!data[0].x.includes(Column)) {
      data[0].x.push(Column);
      data[0].y.push((1 / 3600) * 100);
    }else{
      data[0].y[data[0].x.indexOf(Column)]=data[0].y[data[0].x.indexOf(Column)]+(1 / 3600) * 100;
    }
    
  }

  Plotly.update(chartId, data, layout);
}

// Inizializza i grafici
initializeChart('lightchart', chartData.light.data, chartData.light.layout);
initializeChart('rollchart', chartData.roll.data, chartData.roll.layout);