(function() {
    // Переключение контраста
    function toggleContrast() {
        document.body.classList.toggle('high-contrast');
        localStorage.setItem('highContrast', document.body.classList.contains('high-contrast'));
    }
    // Изменение размера шрифта
    function setFontSize(size) {
        document.body.classList.remove('font-small', 'font-medium', 'font-large', 'font-x-large');
        document.body.classList.add(`font-${size}`);
        localStorage.setItem('fontSize', size);
    }
    // Озвучивание (для доступности)
    window.speak = function(text) {
        if ('speechSynthesis' in window) {
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'ru-RU';
            utterance.rate = 0.9;
            speechSynthesis.cancel();
            speechSynthesis.speak(utterance);
        }
    };

    // Восстановление сохранённых настроек
    if (localStorage.getItem('highContrast') === 'true') document.body.classList.add('high-contrast');
    const savedFont = localStorage.getItem('fontSize');
    if (savedFont) setFontSize(savedFont);

    // Добавляем виджет доступности в навигацию, если его нет
    function addAccessWidget() {
        const nav = document.querySelector('.navbar-nav.ms-auto');
        if (!nav || document.getElementById('accessWidget')) return;
        const widget = document.createElement('li');
        widget.id = 'accessWidget';
        widget.className = 'nav-item dropdown';
        widget.innerHTML = `
            <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                ♿ Доступность
            </a>
            <ul class="dropdown-menu dropdown-menu-end">
                <li><button class="dropdown-item" id="toggleContrastBtn">🌓 Высокий контраст</button></li>
                <li><hr class="dropdown-divider"></li>
                <li><button class="dropdown-item" data-font="small">A- Мелкий шрифт</button></li>
                <li><button class="dropdown-item" data-font="medium">A Средний шрифт</button></li>
                <li><button class="dropdown-item" data-font="large">A+ Крупный шрифт</button></li>
                <li><button class="dropdown-item" data-font="x-large">A++ Очень крупный</button></li>
            </ul>
        `;
        nav.appendChild(widget);
        document.getElementById('toggleContrastBtn')?.addEventListener('click', toggleContrast);
        document.querySelectorAll('[data-font]').forEach(btn => {
            btn.addEventListener('click', (e) => setFontSize(e.target.dataset.font));
        });
    }
    window.addEventListener('DOMContentLoaded', addAccessWidget);
})();