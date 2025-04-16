let rendition;
let book;
let touchStartX = null;
let hasInitiallyRelocated = false;


window.loadBookBase64 = function(base64Data, savedCfi = "", fontSize = "100%", fontFamily = "sans-serif", theme = "light") {
  const binary = atob(base64Data);
  const len = binary.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    bytes[i] = binary.charCodeAt(i);
  }

  const blob = new Blob([bytes], { type: "application/epub+zip" });
  book = ePub(blob);

  rendition = book.renderTo("viewer", {
    width: "100%",
    height: "100%",
    flow: "paginated",
    manager: "continuous"
  });

  if (theme === "dark") {
    rendition.themes.register("dark", {
      body: {
        background: "#121212",
        color: "#ffffff",
        "font-family": fontFamily,
        "font-size": fontSize
      },
      a: {
        color: "#bb86fc"
      }
    });
    rendition.themes.select("dark");
  } else {
    rendition.themes.default({
      body: {
        "font-size": fontSize,
        "font-family": fontFamily,
        background: "#ffffff",
        color: "#000000"
      }
    });
  }

  const fontSizeSelect = document.getElementById("fontSizeSelect");
  if (fontSizeSelect) {
    for (let i = 0; i < fontSizeSelect.options.length; i++) {
      if (fontSizeSelect.options[i].value === fontSize) {
        fontSizeSelect.selectedIndex = i;
        break;
      }
    }
  }

  book.ready.then(() => {
    const key = `bookbuddy_location_${book.key()}`;
    const localLoc = typeof localStorage !== "undefined" ? localStorage.getItem(key) : null;
    const startCfi = savedCfi || localLoc || undefined;
    rendition.display(startCfi);
  });

  rendition.on("relocated", (location) => {
    const cfi = location.start.cfi;
    if (!hasInitiallyRelocated) {
      hasInitiallyRelocated = true;
      console.log("Initial relocation (skipping save):", cfi);
      return;
    }
    console.log("Saving new CFI:", cfi);
    const key = `bookbuddy_location_${book.key()}`;
    if (typeof localStorage !== "undefined") {
      localStorage.setItem(key, cfi);
    }
    if (window.AndroidInterface && AndroidInterface.saveProgress) {
      AndroidInterface.saveProgress(cfi);
    }
  });

  rendition.on("rendered", () => {
    rendition.views().forEach((view) => {
      const doc = view.document;
      doc.addEventListener("touchstart", onTouchStart, false);
      doc.addEventListener("touchend", onTouchEnd, false);
    });
  });

  book.loaded.navigation.then((nav) => {
    const tocDropdown = document.getElementById("tocDropdown");
    tocDropdown.innerHTML = "";

    if (nav.toc.length === 0) {
      const noChapters = document.createElement("option");
      noChapters.textContent = "No chapters found";
      tocDropdown.appendChild(noChapters);
      return;
    }

    nav.toc.forEach((chapter) => {
      const option = document.createElement("option");
      option.textContent = chapter.label;
      option.value = chapter.href;
      tocDropdown.appendChild(option);
    });

    tocDropdown.addEventListener("change", function () {
      const href = tocDropdown.value;
      if (href && book) {
        rendition.display(href);
      }
    });
  }).catch((err) => {
    console.error("Error loading TOC:", err);
  });
};

function nextPage() {
  if (rendition) rendition.next();
}
function prevPage() {
  if (rendition) rendition.prev();
}

document.getElementById("prevTap").addEventListener("click", prevPage);
document.getElementById("nextTap").addEventListener("click", nextPage);

document.getElementById("backBtn").addEventListener("click", function () {
  if (window.AndroidInterface && AndroidInterface.onBackPressed) {
    AndroidInterface.onBackPressed();
  }
});

function onTouchStart(e) {
  touchStartX = e.changedTouches[0].screenX;
}

function onTouchEnd(e) {
  if (!touchStartX) return;
  const dx = e.changedTouches[0].screenX - touchStartX;
  if (dx > 50) prevPage();
  else if (dx < -50) nextPage();
  touchStartX = null;
}

let toolbarTimeout = null;
function showToolbar() {
  document.getElementById("header").style.display = "flex";
  document.getElementById("topGradient").style.display = "block";
  if (toolbarTimeout) clearTimeout(toolbarTimeout);
  toolbarTimeout = setTimeout(() => {
    hideToolbar();
  }, 6000);
}

function hideToolbar() {
  document.getElementById("header").style.display = "none";
  document.getElementById("topGradient").style.display = "none";
}

document.getElementById("topTapZone").addEventListener("click", () => {
  const header = document.getElementById("header");
  const isVisible = header.style.display === "flex";
  if (isVisible) hideToolbar();
  else showToolbar();
});

let swipeStartY = null;
document.addEventListener("touchstart", function (e) {
  swipeStartY = e.changedTouches[0].screenY;
}, false);
document.addEventListener("touchend", function (e) {
  if (swipeStartY === null) return;
  const dy = e.changedTouches[0].screenY - swipeStartY;
  if (dy > 50) showToolbar();
  else if (dy < -50) hideToolbar();
  swipeStartY = null;
}, false);

document.getElementById("fontSizeSelect").addEventListener("change", function () {
  const size = this.value;
  rendition.themes.fontSize(size);
});