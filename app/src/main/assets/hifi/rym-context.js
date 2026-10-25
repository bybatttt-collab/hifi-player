(function () {
  if (!['rateyourmusic.com', 'www.rateyourmusic.com'].includes(location.hostname) ||
      !location.pathname.startsWith('/release/')) return JSON.stringify(null);
  const heading = document.querySelector('.album_title, h1');
  const title = (heading?.textContent || '').replace(/\s+/g, ' ').trim().slice(0, 300);
  const nodes = [...document.querySelectorAll('.album_artist .artist, .album_artist [itemprop="name"], .album_info [itemprop="byArtist"] [itemprop="name"]')];
  let artists = [...new Set(nodes.map(n => n.textContent.trim()).filter(Boolean))].slice(0, 12);
  if (!artists.length) {
    const fallback = document.querySelector('.album_artist')?.textContent.replace(/\s+/g, ' ').trim();
    if (fallback) artists = [fallback.slice(0, 300)];
  }
  return JSON.stringify(title && artists.length ? {title, artists} : null);
})()
