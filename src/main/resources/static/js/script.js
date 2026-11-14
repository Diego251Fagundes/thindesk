// Alternar o menu em telas pequenas.
const menuButton = document.querySelector('.toggle-btn');
const sidebar = document.getElementById('sidebar');

if (menuButton && sidebar) {
    menuButton.setAttribute('aria-controls', 'sidebar');
    menuButton.setAttribute('aria-expanded', 'false');
    menuButton.addEventListener('click', () => {
        const aberto = sidebar.classList.toggle('active');
        menuButton.setAttribute('aria-expanded', String(aberto));
    });
}
