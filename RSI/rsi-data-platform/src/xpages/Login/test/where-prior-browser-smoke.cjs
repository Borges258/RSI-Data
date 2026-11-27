// Browser-only smoke for the restored graph + Run-button page; never clicks Run.
const { spawn } = require('node:child_process');
const fs = require('node:fs');
const os = require('node:os');
const path = require('node:path');

const edge = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const port = 19223;
const profile = fs.mkdtempSync(path.join(os.tmpdir(), 'where-prior-edge-'));
const child = spawn(edge, ['--headless', '--disable-gpu', '--no-first-run', '--no-default-browser-check',
  '--disable-extensions', `--remote-debugging-port=${port}`, `--user-data-dir=${profile}`, 'about:blank'],
{ stdio: 'ignore', windowsHide: true });

const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
let socket;
let sequence = 0;
const waiting = new Map();

async function connect() {
  let pages;
  for (let attempt = 0; attempt < 50; attempt++) {
    try { pages = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json(); break; }
    catch { await pause(200); }
  }
  const page = pages?.find(item => item.type === 'page');
  if (!page) throw new Error('Edge CDP page not ready');
  socket = new WebSocket(page.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => { socket.onopen = resolve; socket.onerror = reject; });
  socket.onmessage = event => {
    const message = JSON.parse(event.data);
    if (message.id && waiting.has(message.id)) {
      const { resolve, reject } = waiting.get(message.id);
      waiting.delete(message.id);
      if (message.error) reject(new Error(message.error.message)); else resolve(message.result);
    }
  };
}

function command(method, params = {}) {
  const id = ++sequence;
  return new Promise((resolve, reject) => {
    waiting.set(id, { resolve, reject });
    socket.send(JSON.stringify({ id, method, params }));
  });
}

async function evaluate(expression) {
  const result = await command('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true });
  if (result.exceptionDetails) throw new Error(result.exceptionDetails.text);
  return result.result.value;
}

async function until(expression) {
  for (let attempt = 0; attempt < 60; attempt++) {
    const value = await evaluate(expression);
    if (value) return value;
    await pause(250);
  }
  throw new Error(`Timed out waiting for ${expression}`);
}

(async () => {
  await connect();
  await command('Page.navigate', { url: 'http://localhost:3000/' });
  const state = await until("(() => { const body = document.body?.innerText ?? ''; const buttons = Array.from(document.querySelectorAll('button')).map(button => button.innerText.trim()); const node = Array.from(document.querySelectorAll('h4')).find(item => item.innerText.includes('等值比较')); return node ? { buttons, graph: true, nodeTop: node.getBoundingClientRect().top, viewportHeight: innerHeight, oldPanel: body.includes('自造小表验证') || body.includes('验证知识注入') } : null; })()");
  if (state.buttons.length !== 1 || state.buttons[0].replace(/\s+/g, '') !== '运行' || state.oldPanel)
    throw new Error(`Unexpected restored page: ${JSON.stringify(state)}`);
  console.log(JSON.stringify({ browser: 'Edge headless', graph: true, runButton: true, oldPanel: false,
    nodeTop: state.nodeTop, viewportHeight: state.viewportHeight }));
})().catch(error => { console.error(error.message); process.exitCode = 1; })
  .finally(() => { socket?.close(); child.kill(); });
