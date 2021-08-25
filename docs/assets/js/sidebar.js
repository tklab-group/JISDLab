const defaultLang = "en";
const text = {
  installation:
    {ja: ["インストール"],
     en: ["Installation"]
    },
  getting_started:
    {ja:
      ["はじめに",
      "Debuggerインスタンスの作成",
      "JPDAを用いたデバッグ",
      "ProbeJを用いたデバッグ",
      "観測値の利用",
      "静的情報の取得",
      "外部プログラムの実行",
      "観測値のエクスポート"],
    en:
      ["Getting started",
      "Create a Debugger instance",
      "Debugging with JPDA",
      "Debugging with ProbeJ",
      "Use of observed values",
      "Getting static information",
      "Running external programs",
      "Export of observed values"]
    }
};

function getText(chapter, lang, index){
  return text[chapter][lang][index];
}

function getFirstElement(id) {
  return document.getElementById(id).firstElementChild;
}

function replaceLangInString(str, from, to) {
  return str.replace("/"+from+"/", "/"+to+"/");
}

function setLang(e, from, to) {
  const href = replaceLangInString(e.getAttribute("href"), from, to);
  e.setAttribute("href", href);
  return href;
}

function createPage() {
  const lang = sessionStorage.getItem('lang') ?? defaultLang;
  if (lang === defaultLang) return;
  const site_title_e = getFirstElement("site-title");
  const installation_e = getFirstElement("installation");
  const getting_started_e = getFirstElement("getting-started");
  const getting_started_items_e = document.getElementById("getting-started-items").children;
  switch (lang) {
    case "en":
      break;
    case "ja":
      break;
    default:
      return;
  }
  setLang(site_title_e, defaultLang, lang);
  setLang(installation_e, defaultLang, lang);
  installation_e.text = getText("installation", lang, 0);
  setLang(getting_started_e, defaultLang, lang);
  getting_started_e.text = getText("getting_started", lang, 0);
  for (let index = 0; index < getting_started_items_e.length; index++) {
    const item_e = getting_started_items_e[index].firstElementChild;
    setLang(item_e, defaultLang, lang);
    item_e.text = getText("getting_started", lang, index+1);
  }
}

function changeLang(lang) {
  const site_title_e = getFirstElement("site-title");
  const home_url = setLang(site_title_e, defaultLang, lang);
  const next_location = replaceLangInString(location.href, defaultLang, lang);
  sessionStorage["lang"] = lang;
  location.href = (next_location === location.href) ? home_url : next_location;
}

createPage();
