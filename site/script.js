// ---- Theme ----

const THEME_KEY = 'kado-theme';

function initTheme() {
  const saved = localStorage.getItem(THEME_KEY);
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
  const theme = saved || (prefersDark ? 'dark' : 'light');
  document.documentElement.setAttribute('data-theme', theme);
}

function toggleTheme() {
  const current = document.documentElement.getAttribute('data-theme');
  const next = current === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  localStorage.setItem(THEME_KEY, next);
}

// ---- Mobile Nav ----

function initNav() {
  const toggle = document.getElementById('nav-toggle');
  const links = document.getElementById('nav-links');

  toggle.addEventListener('click', () => {
    links.classList.toggle('open');
  });

  links.addEventListener('click', (e) => {
    if (e.target.tagName === 'A') {
      links.classList.remove('open');
    }
  });
}

// ---- HTML Escaping ----

const escapeEl = document.createElement('div');
function escapeHtml(str) {
  escapeEl.textContent = str;
  return escapeEl.innerHTML;
}

// ---- Decks ----

let allDecks = [];
let activeCategory = null;

async function loadDecks() {
  try {
    const response = await fetch('decks.json');
    allDecks = await response.json();
    renderCategoryFilters();
    renderDecks(allDecks);
  } catch (err) {
    document.getElementById('deck-grid').innerHTML =
      '<p class="no-results">Failed to load decks. Please try again later.</p>';
  }
}

function renderCategoryFilters() {
  const categories = [...new Set(allDecks.flatMap(d => d.categories))].sort();
  const container = document.getElementById('category-filters');

  const allBtn = document.createElement('button');
  allBtn.className = 'filter-btn active';
  allBtn.textContent = 'All';
  allBtn.addEventListener('click', () => {
    activeCategory = null;
    updateActiveFilter(container, allBtn);
    filterDecks();
  });
  container.appendChild(allBtn);

  categories.forEach(cat => {
    const btn = document.createElement('button');
    btn.className = 'filter-btn';
    btn.textContent = cat;
    btn.addEventListener('click', () => {
      activeCategory = cat;
      updateActiveFilter(container, btn);
      filterDecks();
    });
    container.appendChild(btn);
  });
}

function updateActiveFilter(container, activeBtn) {
  container.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
  activeBtn.classList.add('active');
}

function filterDecks() {
  const filtered = activeCategory
    ? allDecks.filter(d => d.categories.includes(activeCategory))
    : allDecks;
  renderDecks(filtered);
  document.getElementById('no-results').hidden = filtered.length > 0;
}

function renderDecks(decks) {
  const grid = document.getElementById('deck-grid');
  grid.innerHTML = decks.map(deck => `
    <article class="deck-card">
      <h3 class="deck-title">${escapeHtml(deck.title)}</h3>
      <p class="deck-description">${escapeHtml(deck.description)}</p>
      <div class="deck-tags">
        ${deck.categories.map(c => `<span class="tag">${escapeHtml(c)}</span>`).join('')}
      </div>
      <div class="deck-meta">
        ${deck.cardCount ? `<span>${deck.cardCount} cards</span>` : ''}
        ${deck.author ? `<span>by ${escapeHtml(deck.author)}</span>` : ''}
      </div>
      <a href="${escapeHtml(deck.downloadUrl)}" class="download-btn" target="_blank" rel="noopener">
        Download .apkg
      </a>
    </article>
  `).join('');
}

// ---- Init ----

document.addEventListener('DOMContentLoaded', () => {
  initTheme();
  initNav();
  document.getElementById('theme-toggle').addEventListener('click', toggleTheme);
  loadDecks();
});
