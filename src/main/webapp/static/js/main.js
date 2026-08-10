'use strict';

/* ============================================================
   THEME
   ============================================================ */
const THEME_KEY = 'cr-theme';

function getTheme() {
    return localStorage.getItem(THEME_KEY)
        || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
}

function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(THEME_KEY, theme);

    const btn = document.getElementById('theme-toggle');
    if (btn) {
        btn.textContent = theme === 'dark' ? '☀️' : '🌙';
        btn.setAttribute('aria-label', theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme');
    }
}

function toggleTheme() {
    applyTheme(getTheme() === 'dark' ? 'light' : 'dark');
}

/* ============================================================
   MOBILE NAV
   ============================================================ */
function initMobileNav() {
    const burger = document.getElementById('nav-burger');
    const nav = document.getElementById('nav-links');
    if (!burger || !nav) return;

    burger.addEventListener('click', () => {
        const open = nav.classList.toggle('open');
        burger.setAttribute('aria-expanded', open);
    });

    // Close on outside click
    document.addEventListener('click', (e) => {
        if (!burger.contains(e.target) && !nav.contains(e.target)) {
            nav.classList.remove('open');
            burger.setAttribute('aria-expanded', false);
        }
    });
}

/* ============================================================
   LANGUAGE SWITCHER
   Sends locale change to server via POST, server saves to session.
   ============================================================ */
function changeLocale(locale) {
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/locale';

    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'locale';
    input.value = locale;

    const redirect = document.createElement('input');
    redirect.type = 'hidden';
    redirect.name = 'redirect';
    redirect.value = window.location.pathname + window.location.search;

    form.appendChild(input);
    form.appendChild(redirect);
    document.body.appendChild(form);
    form.submit();
}

/* ============================================================
   AUTO-DISMISS ALERTS
   ============================================================ */
function initAlerts() {
    document.querySelectorAll('.alert[data-dismiss]').forEach(alert => {
        const delay = parseInt(alert.dataset.dismiss) || 4000;
        setTimeout(() => {
            alert.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-8px)';
            setTimeout(() => alert.remove(), 400);
        }, delay);
    });
}

/* ============================================================
   BOOKING FORM — live price calculation
   ============================================================ */
function initBookingForm() {
    const dateFrom = document.getElementById('dateFrom');
    const dateTo   = document.getElementById('dateTo');
    const priceEl  = document.getElementById('calculated-price');
    const pricePerDay = parseFloat(document.getElementById('price-per-day')?.dataset.price);

    if (!dateFrom || !dateTo || !priceEl || isNaN(pricePerDay)) return;

    function calculate() {
        const from = new Date(dateFrom.value);
        const to   = new Date(dateTo.value);
        if (isNaN(from) || isNaN(to) || to <= from) {
            priceEl.textContent = '—';
            return;
        }
        const days = Math.round((to - from) / (1000 * 60 * 60 * 24));
        const total = (days * pricePerDay).toFixed(2);
        priceEl.textContent = `${total} BYN (${days} ${pluralDays(days)})`;
    }

    dateFrom.addEventListener('change', calculate);
    dateTo.addEventListener('change', calculate);

    // Min date = today
    const today = new Date().toISOString().split('T')[0];
    dateFrom.min = today;
    dateTo.min = today;

    dateFrom.addEventListener('change', () => {
        if (dateTo.value && dateTo.value <= dateFrom.value) {
            dateTo.value = '';
            priceEl.textContent = '—';
        }
        dateTo.min = dateFrom.value || today;
    });
}

function pluralDays(n) {
    if (n % 10 === 1 && n % 100 !== 11) return 'день';
    if ([2,3,4].includes(n % 10) && ![12,13,14].includes(n % 100)) return 'дня';
    return 'дней';
}

/* ============================================================
   STAR RATING INPUT
   ============================================================ */
function initStarRating() {
    const container = document.querySelector('.star-input');
    if (!container) return;

    const stars = container.querySelectorAll('.star-input__star');
    const input = container.querySelector('input[type="hidden"]');

    stars.forEach((star, i) => {
        star.addEventListener('click', () => {
            input.value = i + 1;
            stars.forEach((s, j) => s.classList.toggle('active', j <= i));
        });

        star.addEventListener('mouseenter', () => {
            stars.forEach((s, j) => s.classList.toggle('hover', j <= i));
        });
    });

    container.addEventListener('mouseleave', () => {
        stars.forEach(s => s.classList.remove('hover'));
    });
}

/* ============================================================
   CLIENT-SIDE FORM VALIDATION
   ============================================================ */
function initFormValidation() {
    document.querySelectorAll('form[data-validate]').forEach(form => {
        form.addEventListener('submit', (e) => {
            let valid = true;

            form.querySelectorAll('[required]').forEach(field => {
                field.classList.remove('is-invalid');
                if (!field.value.trim()) {
                    field.classList.add('is-invalid');
                    valid = false;
                }
            });

            const email = form.querySelector('input[type="email"]');
            if (email && email.value && !email.value.includes('@')) {
                email.classList.add('is-invalid');
                valid = false;
            }

            const password = form.querySelector('input[name="password"]');
            if (password && password.value && password.value.length < 8) {
                password.classList.add('is-invalid');
                valid = false;
            }

            if (!valid) {
                e.preventDefault();
                form.querySelector('.is-invalid')?.focus();
            }
        });

        // Clear error state on input
        form.querySelectorAll('.form-control').forEach(field => {
            field.addEventListener('input', () => field.classList.remove('is-invalid'));
        });
    });
}

/* ============================================================
   INIT
   ============================================================ */
document.addEventListener('DOMContentLoaded', () => {
    // Apply saved theme before paint to avoid flash
    applyTheme(getTheme());

    const toggleBtn = document.getElementById('theme-toggle');
    if (toggleBtn) toggleBtn.addEventListener('click', toggleTheme);

    initMobileNav();
    initAlerts();
    initBookingForm();
    initStarRating();
    initFormValidation();
});

// Apply theme immediately (before DOMContentLoaded) to avoid flash of wrong theme
applyTheme(getTheme());