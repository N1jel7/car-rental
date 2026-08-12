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

    const setActive = (count) => {
        stars.forEach((s, j) => s.classList.toggle('active', j < count));
    };

    // Reflect the current value on load so the stars aren't blank.
    setActive(parseInt(input.value, 10) || 0);

    stars.forEach((star, i) => {
        star.addEventListener('click', () => {
            input.value = i + 1;
            setActive(i + 1);
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
   CAR DETAIL — PHOTO CAROUSEL
   ============================================================ */
let carGalleryIndex = 0;
let carGallerySlideCount = 0;

function initCarGallery() {
    const track = document.getElementById('car-gallery-track');
    if (!track) return;

    carGallerySlideCount = track.children.length;
    carGalleryIndex = 0;

    let touchStartX = null;
    track.addEventListener('touchstart', (e) => {
        touchStartX = e.touches[0].clientX;
    }, { passive: true });

    track.addEventListener('touchend', (e) => {
        if (touchStartX === null) return;
        const delta = e.changedTouches[0].clientX - touchStartX;
        if (Math.abs(delta) > 40) {
            delta < 0 ? carGalleryNext() : carGalleryPrev();
        }
        touchStartX = null;
    });
}

function carGalleryRender() {
    const track = document.getElementById('car-gallery-track');
    const counter = document.getElementById('car-gallery-counter');
    if (!track) return;

    track.style.transform = `translateX(-${carGalleryIndex * 100}%)`;

    if (counter) {
        counter.innerHTML = `${carGalleryIndex + 1} / ${carGallerySlideCount}`;
    }

    document.querySelectorAll('.car-gallery__thumb').forEach((thumb, i) => {
        thumb.classList.toggle('active', i === carGalleryIndex);
    });
}

function carGalleryGoTo(index) {
    if (carGallerySlideCount === 0) return;
    carGalleryIndex = (index + carGallerySlideCount) % carGallerySlideCount;
    carGalleryRender();
}

function carGalleryNext() {
    carGalleryGoTo(carGalleryIndex + 1);
}

function carGalleryPrev() {
    carGalleryGoTo(carGalleryIndex - 1);
}

/* ============================================================
   ADMIN — LIVE PHOTO PREVIEW ON UPLOAD
   ============================================================ */
function initNewImagePreview() {
    const input = document.getElementById('new-images-input');
    const preview = document.getElementById('new-images-preview');
    if (!input || !preview) return;

    // If an existing (already-saved) photo is already the cover, don't silently steal
    // that spot just because new files were picked — only an explicit click should.
    const hasExistingPrimary = !!document.querySelector(
        'input[name="primarySelection"][value^="existing:"]');

    let primaryIndex = hasExistingPrimary ? -1 : 0;

    input.addEventListener('change', () => {
        primaryIndex = hasExistingPrimary ? -1 : 0;
        renderNewImagePreview();
    });

    function renderNewImagePreview() {
        preview.innerHTML = '';

        const files = Array.from(input.files).filter(f => f.type.startsWith('image/'));
        if (primaryIndex >= files.length) primaryIndex = files.length > 0 && !hasExistingPrimary ? 0 : -1;

        Array.from(input.files).forEach((file, index) => {
            if (!file.type.startsWith('image/')) return;

            const url = URL.createObjectURL(file);
            const isPrimary = index === primaryIndex;

            const item = document.createElement('div');
            item.className = 'image-manager-item' + (isPrimary ? ' image-manager-item--primary' : '');

            const img = document.createElement('img');
            img.src = url;
            img.alt = file.name;
            img.onload = () => URL.revokeObjectURL(url);

            const primaryLabel = document.createElement('label');
            primaryLabel.className = 'image-manager-item__primary';

            const radio = document.createElement('input');
            radio.type = 'radio';
            radio.name = 'primarySelection';
            radio.value = 'new:' + index;
            radio.checked = isPrimary;
            radio.addEventListener('change', () => {
                primaryIndex = index;
                renderNewImagePreview();
            });

            const radioLabel = document.createElement('span');
            radioLabel.textContent = 'Главное';

            primaryLabel.appendChild(radio);
            primaryLabel.appendChild(radioLabel);

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.className = 'image-manager-item__remove';
            removeBtn.textContent = '× Убрать';
            removeBtn.addEventListener('click', () => removeSelectedFile(index));

            item.appendChild(img);
            item.appendChild(primaryLabel);
            item.appendChild(removeBtn);
            preview.appendChild(item);
        });
    }

    function removeSelectedFile(index) {
        if (index === primaryIndex) {
            primaryIndex = hasExistingPrimary ? -1 : 0;
        } else if (index < primaryIndex) {
            primaryIndex -= 1;
        }

        const dataTransfer = new DataTransfer();
        Array.from(input.files).forEach((file, i) => {
            if (i !== index) dataTransfer.items.add(file);
        });
        input.files = dataTransfer.files;
        renderNewImagePreview();
    }
}

/* ============================================================
   NUMBER INPUT — themed up/down steppers
   ============================================================ */
function initNumberSteppers() {
    document.querySelectorAll('input[type="number"].form-control').forEach((input) => {
        if (input.closest('.number-input')) return;

        const wrapper = document.createElement('div');
        wrapper.className = 'number-input';
        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        const controls = document.createElement('div');
        controls.className = 'number-input__controls';

        const upBtn = document.createElement('button');
        upBtn.type = 'button';
        upBtn.className = 'number-input__btn number-input__btn--up';
        upBtn.setAttribute('aria-label', 'Increase value');
        upBtn.tabIndex = -1;

        const downBtn = document.createElement('button');
        downBtn.type = 'button';
        downBtn.className = 'number-input__btn number-input__btn--down';
        downBtn.setAttribute('aria-label', 'Decrease value');
        downBtn.tabIndex = -1;

        const stepOf = () => parseFloat(input.step) || 1;
        const decimalsOf = (step) => (String(step).includes('.') ? String(step).split('.')[1].length : 0);

        const changeValue = (delta) => {
            const step = stepOf();
            const current = parseFloat(input.value) || 0;
            let next = current + delta;

            if (input.min !== '') next = Math.max(next, parseFloat(input.min));
            if (input.max !== '') next = Math.min(next, parseFloat(input.max));

            input.value = next.toFixed(decimalsOf(step));
            input.dispatchEvent(new Event('input', { bubbles: true }));
            input.dispatchEvent(new Event('change', { bubbles: true }));
        };

        upBtn.addEventListener('click', () => changeValue(stepOf()));
        downBtn.addEventListener('click', () => changeValue(-stepOf()));

        controls.appendChild(upBtn);
        controls.appendChild(downBtn);
        wrapper.appendChild(controls);
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
    initCarGallery();
    initNewImagePreview();
    initNumberSteppers();
    initFormValidation();
});

// Apply theme immediately (before DOMContentLoaded) to avoid flash of wrong theme
applyTheme(getTheme());