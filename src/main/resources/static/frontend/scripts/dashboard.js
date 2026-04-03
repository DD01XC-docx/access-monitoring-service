const CHART_DEFAULT_THEME = {
    mode: 'dark',
    colors: {
        success: '#22c55e',
        warning: '#f59e0b',
        danger: '#ef4444',
        info: '#3b82f6',
        grid: 'rgba(150, 150, 180, 0.1)'
    }
};

const RANGE_CONFIG = {
    '1h': 'Last hour',
    '24h' : 'Last 24 hours',
    '7d' : 'Last 7 days'
};

const BASED_CHART_OPTIONS = {
    chart: {
        height: 280,
        background: 'transparent',
        toolbar: {show:false},
        animations: {enabled: true,speed:500},
    },
    theme: { mode: CHART_DEFAULT_THEME.mode },
    grid: {
        borderColor: CHART_DEFAULT_THEME.colors.grid,
        strokeDashArray: 4 
    },
    dataLabels: { enabled: false }
};

const CHART_CONFIG = [
    {
        id: '#total-inc-card',
        key: 'accessChart',
        type: 'area',
        url: '/api/access/stat/hourly',
        hasRange: true
    },
    {
        id: '#sla-compliance',
        url: '/api/access/stat/sla',
        type: 'radialBar',
        key: "slaChart"
    },
    {
        id: '#alert-levels',
        url: '/api/access/stat/alert-lvls',
        type: 'line',
        key: 'alertLevelsChart',
        hasRange: true
    },
    {
        id: '#top-failed-accounts',
        url: '/api/access/stat/top-failed',
        type: 'bar',
        key: 'topFailedChart'
    },
    {
        id: '#agent-status',
        url:'/api/access/stat/agent-status',
        type: 'treemap',
        key: 'agentChart'
    },
    {
        id: '#system-status-bar',
        url: '/api/access/stat/health',
        type: 'bar',
        key: 'dbHealthChart'
    },
    {
        id: '#responce-time-сhart',
        url: '/api/access/stat/responce-time',
        type: 'boxPlot',
        key: 'reponceTimeDistrib'
    }
];

const TYPE_PRESETS = {
    area: {
        colors: [CHART_DEFAULT_THEME.colors.success, CHART_DEFAULT_THEME.colors.warning],
        stroke: {curve: 'smooth', width: 3},
        fill: {type: 'gradient', gradient: {opacityFrom: 0.5, opacityTo: 0.1}}
    },
    radialBar: {
        colors: [CHART_DEFAULT_THEME.colors.info],
        plotOptions: {
            radialBar: {
                hollow: {size: '85%'},
                dataLabels: {
                    name: {show: true, color: '#0156ce', fontSize: '25px'},
                    value: {show: true, color : '#0156ce', fontSize: '25px'}
                }
            }
        }
    },
    bar: {
        colors: [CHART_DEFAULT_THEME.colors.danger],
        plotOptions: { bar: { horizontal: true, borderRadius: 4, barHeight: '60%' } },
        dataLabels: {enabled: true}
    },
    treemap: {
        colors: ['#38bff8a2', '#17fa0795'],
        plotOptions: {
            treemap: {
                distributed: true,
                enableShades: false,
                borderRadius: 5
            }
        },
        stroke: {
            show: true,
            width: 1,
            colors: ['rgba(15,20,40,0.5)']
        },
        dataLabels: {
            enabled: true,
            style: {
                fontSize : '10px',
                fontWeight: 500
            },
            formatter: function(text, opts) {
                return text + ": " + opts.value;
            }
        },
        tooltip: {
            theme: 'dark',
            y: {
                formatter: function(val) {
                    return val + ' agents';
                }
            }
        }
    },
    line: {
        colors: ['#38bdf8', '#fff200', '#dc465c', '#fa0707'],
        stroke: {
            curve: 'smooth',
            width: 2
        },
        markers: {
            size: 2,
            hover: {
                size: 5
            }
        },
        legend: {
            show: true,
            position: 'top',
            horizontalAlign: 'left'
        },
        fill: {
            opacity: 0.7
        },
        xaxis: {
            type: 'category',
            labels: {
                style: {
                    colors: '#fff'
                }
            }
        },
        yaxis: {
            labels: {
                style: {
                    colors: '#fff'
                }
            }
        },
        tooltip: {
            theme: 'dark',
            y: {
                formatter: function(val) {
                    return val + ' failed attempts';
                }
            }
        }
    },
    boxPlot: {
        colors: ['green', 'orange'],
        plotOptions: {
            boxPlot: {
                colors: {
                    upper: '#00E396',
                    lower: '#00B57A'
                }
            }
        },
        stroke: {
            colors: ['#ffffff']
        }
    }
};

function renderChartOptions(data, config) {

    let options = structuredClone(BASED_CHART_OPTIONS);

    const defaultPreset = TYPE_PRESETS[config.type] || {};

    options = { ...options, ...defaultPreset };
    options.chart.type = config.type;
    options.series = data.series || data;

     if (defaultPreset.plotOptions) {
        options.plotOptions = defaultPreset.plotOptions;
    }

    if (defaultPreset.tooltip) {
        options.tooltip = defaultPreset.tooltip;
    }

    if (defaultPreset.stroke) {
        options.stroke = defaultPreset.stroke;
    }

    if (defaultPreset.fill) {
        options.fill = defaultPreset.fill;
    }

    if (config.key === 'dbHealthChart') {
    options.series = [{
        name: 'Status',
        data: data
    }];
}
    if (data.categories) {
         const isDense24hAxis = config.type === 'area' && data.categories.length >= 20;
        options.xaxis = {
            ...options.xaxis,
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

    if (config.options) {

        Object.keys(config.options).forEach(key => {
            if (typeof config.options[key] === 'object' && options[key]) {
                options[key] = { ...options[key], ...config.options[key] };
            } else {
                options[key] = config.options[key];
            }
        });
    }
    return options;
}

let currentRange = '24h';
const chartInstances = new Map();   


async function initDashboard() {
    document.getElementById('access-range')?.addEventListener('change', (e) => {
        currentRange = e.target.value;
        refreshDashboard(); 
    });
    await refreshDashboard();
}

function buildRequestUrl(config, currentRange) {
    let url = config.url;
    if (config.hasRange) {
         const separator = url.includes('?') ? '&' : '?';
        url = url + separator + 'range=' + currentRange;
    }
    return url;
}

async function syncChart(config) {
    try {
        const data = await fetchData(config, currentRange);
        const options = renderChartOptions(data, config);
        const el = document.querySelector(config.id);
        if (!el) {
            console.warn(`Element ${config.id} not found for chart ${config.key}`);
            return;
        }
        const existingChart = chartInstances.get(config.key);
        if (existingChart) {
            await existingChart.updateOptions(options);
        } else {
            const chart = new ApexCharts(el, options);
            await chart.render();
            chartInstances.set(config.key, chart);
        }
    } catch (error) {console.error('SyncChart error:', error);}
}

async function fetchData(config, currentRange) {
    const url = buildRequestUrl(config, currentRange);
    const token = localStorage.getItem('jwt'); 
    if (!token) {
        window.location.href = '/login.html';
        return;
    }
    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    if (!response.ok) {
        if (response.status === 401 || response.status === 403) {
            localStorage.removeItem('jwt');
            window.location.href = '/login.html';
        }
        throw new Error('Request failed: ' + response.status);
    }
    return await response.json();
}

async function refreshDashboard() {
    await Promise.all([
        ...CHART_CONFIG.map(config => syncChart(config)),
        loadRecentLogs()
    ]);
}

async function loadRecentLogs() {
    const list = document.getElementById('logs-list');
    if (!list) return;

    try {
        const logs = await fetchData({ url: '/api/access/logs/recent' }, currentRange);

        if (!Array.isArray(logs) || logs.length === 0) {
            list.innerHTML = '<div class="log-empty">No logs found yet.</div>';
            return;
        }

        list.innerHTML = logs.map(log => {
            const status = (log.status || 'UNKNOWN').toUpperCase();
            const statusClass = status === 'SUCCESS' ? 'log-status-success' : 'log-status-failed';
            const createdAt = formatLogDate(log.createdAt);
            const user = escapeHtml(log.usernameOrEmail || 'unknown');
            const ip = escapeHtml(log.ipAddress || 'n/a');

            return `
                <div class="log-item">
                    <div class="log-user">
                        <span class="log-label">User</span>
                        <span class="log-value">${user}</span>
                    </div>
                    <div class="log-status">
                        <span class="log-label">Status</span>
                        <span class="log-status-badge ${statusClass}">${escapeHtml(status)}</span>
                    </div>
                    <div class="log-ip">
                        <span class="log-label">IP Address</span>
                        <span class="log-value">${ip}</span>
                    </div>
                    <div class="log-time">
                        <span class="log-label">Created</span>
                        <span class="log-value">${escapeHtml(createdAt)}</span>
                    </div>
                </div>
            `;
        }).join('');
    } catch (error) {
        list.innerHTML = '<div class="log-empty">Failed to load logs.</div>';
        console.error('Log load error:', error);
    }
}

function formatLogDate(value) {
    if (!value) return 'n/a';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
}

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

function toggleLogsPanel() {
    const card = document.getElementById('logs-card');
    const button = document.getElementById('logs-toggle');
    if (!card || !button) return;

    card.classList.toggle('collapsed');
    button.textContent = card.classList.contains('collapsed') ? 'Expand' : 'Collapse';
}

function scrollToLogs() {
    const logsSection = document.querySelector('.logs-section');
    if (!logsSection) return;
    logsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function logout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    window.location.href = '/login.html';
}

function openProfilePlaceholder() {
    window.location.href = '/documentation.html';
}

setInterval(refreshDashboard, 20000);
document.addEventListener('DOMContentLoaded', initDashboard);
