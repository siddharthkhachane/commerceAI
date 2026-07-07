export function ThemeScript() {
  const script = `
    (function () {
      try {
        var theme = localStorage.getItem('theme');
        var prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        if (theme === 'dark' || (theme !== 'light' && prefersDark)) {
          document.documentElement.classList.add('dark');
        }
      } catch (e) {}
    })();
  `;

  return <script dangerouslySetInnerHTML={{ __html: script }} />;
}
