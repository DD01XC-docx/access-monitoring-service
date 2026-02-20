document.addEventListener('DOMContentLoaded', async function() {
    await initDashboard();
});
async function initDashboard() {
    const accessRangeSelect = document.getElementById('access-range');
    let currentRange = accessRangeSelect ? accessRangeSelect.value : '24h';
    //graph-configs
    const chartConfigs = [
    { id: "#total-inc-card", url: "/api/access/stat/hourly", type: "area", key: "accessChart" },
    { id: "#sla-compliance", url: "/api/access/stat/sla", type: "radialBar", key: "slaChart" },
    { id: "#top-failed-accounts", url: "/api/access/stat/top-failed", type: "bar", key: "topFailedChart" },
    { id: "#agent-status", url: "/api/access/stat/agent-status", type: "treemap", key: "agentChart" }
    ];
    //refresh
    const refreshData = async () => {
        for(const config of chartConfigs) {
            try {
                const requestUrl = config.key === 'accessChart'
                    ? `${config.url}?range=${encodeURIComponent(currentRange)}`
                    : config.url;
                const res = await fetch(requestUrl);
                if (!res.ok) {
                    console.error('Request failed:', config.url, res.status);
                    continue;
                }
                const data = await res.json();
                const chartData = data && (data.series || data);

                if (!chartData) {
                    console.error('Empty chart data:', config.url);
                    continue;
                }

            if (!window[config.key]) {
                const extra = config.type === 'radialBar' ?
                {labels: ['SLA Success'], colors: ['green']} :
                config.type === 'bar' ?
                { colors: ['#ef4444'], horizontal: true } :
                {};
                    window[config.key] = renderChart(config.id, config.type, chartData, data.categories, extra);
            } else {
                window[config.key].updateSeries(chartData);
                    if (data.categories) {
                            let xaxisOptions = { categories: data.categories };

                            if (config.key === 'accessChart') {
                                const isDense24hAxis = Array.isArray(data.categories) && data.categories.length >= 20;
                                xaxisOptions = {
                                    categories: data.categories,
                                    tickAmount: isDense24hAxis ? 12 : undefined,
                                    labels: {
                                        show: true,
                                        rotate: isDense24hAxis ? -45 : 0,
                                        hideOverlappingLabels: true,
                                        trim: false
                                    },
                                    tickPlacement: 'on',
                                    tooltip: { enabled: false }
                                };
                            }

                            window[config.key].updateOptions({
                                xaxis: xaxisOptions
                            });
                }
                }
            } catch (e) {
                    console.error('Update error:', e);
            }
        }
    };
    if (accessRangeSelect) {
        accessRangeSelect.addEventListener('change', async function (e) {
            currentRange = e.target.value;
            await refreshData();
        });
    }
    initAlertLevelsCard();
    await refreshData();
}

function initAlertLevelsCard() {
    // alert levels
    const dateInput = document.getElementById('heatmap-date');
    const prevBtn = document.getElementById('heatmap-prev-day');
    const nextBtn = document.getElementById('heatmap-next-day');

    if (!dateInput) {
        return;
    }

    let selectedDate = new Date(); // today
    dateInput.value = formatDateForInput(selectedDate);

    const renderHeatmapForDate = () => {
        const heatmapSeries = generateHeatmapData(selectedDate);

        if (!window.alertLevelsChart) {
            window.alertLevelsChart = renderChart('#alert-levels', 'heatmap', heatmapSeries);
            return;
        }

        window.alertLevelsChart.updateSeries(heatmapSeries);
    };

    dateInput.addEventListener('change', function (e) {
        const parsedDate = new Date(e.target.value);

        if (!isNaN(parsedDate.getTime())) {
            selectedDate = parsedDate;
            renderHeatmapForDate();
        }
    });

    if (prevBtn) {
        prevBtn.addEventListener('click', function () {
            selectedDate.setDate(selectedDate.getDate() - 1);
            dateInput.value = formatDateForInput(selectedDate);
            renderHeatmapForDate();
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', function () {
            selectedDate.setDate(selectedDate.getDate() + 1);
            dateInput.value = formatDateForInput(selectedDate);
            renderHeatmapForDate();
        });
    }

    renderHeatmapForDate();
}

function formatDateForInput(dateObj) {
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function renderChart(containerId, type, series, categories = null, customOptions = {}) {
    const isArea = type === 'area';
    const baseOptions = {
        chart: {
            type: type,
            height: type === 'radialBar' ? 250 : 350,
            background: 'transparent',
            animations: {
                enabled: true,
                easing: 'linear',
                speed: 250,
                dynamicAnimation: {
                    enabled: true,
                    speed: 250
                }
            },
            toolbar: {
                show: false
            }
        },
        theme: { mode: 'dark' },
        series: series,
        colors: customOptions.colors || ['#22c55e', '#f59e0b'],
        stroke: type === 'donut' ? { colors: ['transparent'] } : { curve: 'smooth', width: 3 },
        labels: customOptions.labels || [],
        annotations: {},
        dataLabels: { enabled: false },
        yaxis: {
            labels: {
                formatter: function (val) {
                    return typeof val === 'number' ? val.toFixed(0) : val;
                }
            }
        },
        grid: {
            borderColor: 'rgba(148, 163, 184, 0.2)',
            strokeDashArray: 4
        },
        legend: {
            position: 'top',
            labels: { colors: '#cbd5e1' }
        },
        tooltip: {
            theme: 'dark'
        }
    };

    //area
    if (isArea) {
        baseOptions.markers = {
            size: 2,
            hover: { size: 5 }
        };
        baseOptions.fill = {
            type: 'gradient',
            gradient: {
                shadeIntensity: 1,
                opacityFrom: 0.35,
                opacityTo: 0.05,
                stops: [0, 90, 100]
            }
        };
    }
    //categories
    if (categories) {
        const isDense24hAxis = type === 'area' && categories.length >= 20;
        baseOptions.xaxis = {
            categories: categories,
            tickAmount: isDense24hAxis ? 12 : undefined,
            labels: {
                show: true,
                rotate: isDense24hAxis ? -45 : 0,
                hideOverlappingLabels: true,
                trim: false
            },
            tickPlacement: 'on',
            tooltip: { enabled: false }
        };
    }
    //radial
    if (type === 'radialBar') {
        baseOptions.plotOptions = {
            radialBar: {
                hollow: {size: '85%'},
                dataLabels: {
                name: {show: true, color: '#0156ce', fontSize: '25px'},
                value: {show: true, color: '#0156ce', fontSize: '25px'}
                }
            }
        };
        baseOptions.labels = customOptions.labels || [];
    }
    //bar
    if (type === 'bar') {
        baseOptions.plotOptions = {
            bar: {
                horizontal: customOptions.horizontal === true,
                borderRadius: 4,
                barHeight: '60%'
            }
        };
        baseOptions.dataLabels = { enabled: true };
    }
    //heatmap
    if (type === 'heatmap') {
    baseOptions.plotOptions = {
        heatmap: {
            shadeIntensity: 0,
            radius: 4,
            useFillColorAsStroke: false,
            colorScale: {
                ranges: [
                    { from: 0, to: 10, name: 'Low', color: '#2D93AD' },
                    { from: 11, to: 40, name: 'Medium', color: '#F7B84B' },
                    { from: 41, to: 70, name: 'High', color: '#F06548' },
                    { from: 71, to: 100, name: 'Critical', color: '#B11E31' }
                ]
            }
        }
    };
    baseOptions.dataLabels = {enabled: false};
    }
    //treemap
    if (type === 'treemap') {
    baseOptions.chart.height = 300;
    const legendObj = {
        show: false
    };
    baseOptions.legend = legendObj;
    if (customOptions.colors != null) {
        baseOptions.colors = customOptions.colors;
    } else {
        baseOptions.colors = ['green', 'orange', 'red', 'blue'];
    }
    baseOptions.stroke = {};
    baseOptions.stroke.show = true;
    baseOptions.stroke.width = 1;
    baseOptions.stroke.colors = ['rgba(15,20,40,0.50)'];
    baseOptions.dataLabels = {
        enabled: true,
        style: {
            fontSize: '10px',
            fontWeight: 600
        },
        formatter: function(text, opts) {
            return text + ": " + opts.value;
        }
    };
    baseOptions.plotOptions = {
        treemap: {
            distributed: true,
            enableShades: false,
            borderRadius: 6
        }
    };
    baseOptions.tooltip = {
        theme: 'dark',
        y: {
            formatter: function(val) {
                return val + " agents";
            }
        }
    };
}
    const chartElement = document.querySelector(containerId);
    if (chartElement) {
        const chart = new ApexCharts(chartElement, baseOptions);
        chart.render();
        return chart;
    }
}
function updateCards(data) {
    const totalEl = document.getElementById('total-inc');
    const openEl = document.getElementById('open-inc');
    
    if (totalEl) totalEl.innerHTML = `<h3>${data.total}</h3><p>Total Access</p>`;
    if (openEl) openEl.innerHTML = `<h3>${data.open}</h3><p>Open Incidents</p>`;
}
//heatmap-func
function generateHeatmapData(dateObj = new Date()) {
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const dateSeed =
        dateObj.getFullYear() * 10000 +
        (dateObj.getMonth() + 1) * 100 +
        dateObj.getDate();
    const getValue = (dayIndex, hourIndex) => {
        const seed = dateSeed + dayIndex * 37 + hourIndex * 17;
        return (seed * 13) % 51;
    };

    return days.map(day => ({
        name: day,
        data: Array.from({ length: 5 }, (_, i) => ({
            x: `${i}:00`,
            y: getValue(days.indexOf(day), i)
        }))
    }));
}