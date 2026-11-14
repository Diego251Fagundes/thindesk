(() => {
    const container = document.getElementById('evolution-chart');
    const rows = Array.from(document.querySelectorAll('.chart-data tbody tr'));
    if (!container || !rows.length) return;
    const data = rows.map(row => ({ date: row.dataset.dia, opened: Number(row.dataset.aberturas), resolved: Number(row.dataset.resolucoes) }));
    const ns = 'http://www.w3.org/2000/svg';
    const element = (tag, attributes, text) => {
        const node = document.createElementNS(ns, tag);
        Object.entries(attributes).forEach(([key, value]) => node.setAttribute(key, value));
        if (text !== undefined) node.textContent = text;
        return node;
    };
    let lastWidth = 0;
    function draw() {
        const width = Math.max(280, container.clientWidth);
        if (width === lastWidth) return;
        lastWidth = width;
        const right = width - 18;
        const svg = element('svg', { viewBox: `0 0 ${width} 240`, 'aria-hidden': 'true' });
        const step = Math.max(1, Math.ceil(Math.max(...data.map(d => Math.max(d.opened, d.resolved))) / 4));
        const maximum = step * 4;
        const x = index => 34 + index / Math.max(1, data.length - 1) * (right - 34);
        const y = value => 203 - value / maximum * 180;
        for (let tick = 0; tick <= 4; tick++) {
            const value = tick * step;
            svg.append(element('line', { x1: 34, x2: right, y1: y(value), y2: y(value), stroke: '#e8edf4', 'stroke-dasharray': '3 5' }));
            svg.append(element('text', { x: 23, y: y(value) + 4, 'text-anchor': 'end', fill: '#57677c', 'font-size': 12 }, value));
        }
        [0, Math.round((data.length - 1) / 3), Math.round((data.length - 1) * 2 / 3), data.length - 1].forEach(index => {
            svg.append(element('text', { x: x(index), y: 230, 'text-anchor': index === 0 ? 'start' : index === data.length - 1 ? 'end' : 'middle', fill: '#57677c', 'font-size': 12 }, data[index].date));
        });
        const openedPoints = data.map((d, index) => `${x(index)},${y(d.opened)}`).join(' ');
        svg.append(element('polygon', { points: `34,203 ${openedPoints} ${right},203`, fill: '#eaf0fc' }));
        [['opened', '#255cce', 'Aberturas'], ['resolved', '#147d68', 'Resoluções']].forEach(([key, color, label]) => {
            svg.append(element('polyline', { points: data.map((d, index) => `${x(index)},${y(d[key])}`).join(' '), fill: 'none', stroke: color, 'stroke-width': 2.5, 'stroke-linejoin': 'round', 'stroke-dasharray': key === 'resolved' ? '6 4' : 'none' }));
            data.forEach((d, index) => {
                if (!d[key]) return;
                const circle = element('circle', { cx: x(index), cy: y(d[key]), r: 4, fill: color, stroke: 'white', 'stroke-width': 2 });
                circle.append(element('title', {}, `${d.date} · ${label}: ${d[key]}`));
                svg.append(circle);
            });
        });
        container.replaceChildren(svg);
    }
    draw();
    if ('ResizeObserver' in window) new ResizeObserver(draw).observe(container);
    else window.addEventListener('resize', draw);
})();
