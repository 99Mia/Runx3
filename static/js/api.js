// /js/api.js
(function () {
    // (선택) CSRF 메타 태그에서 읽기 — Security 켜져 있으면 사용
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    async function handle(res, method, url) {
        if (!res.ok) {
            let msg = `요청 실패 (${res.status})`;
            try { const j = await res.json(); if (j?.message) msg = j.message; } catch {}
            throw new Error(msg);
        }
        // 204 등 비본문 응답 대비
        const text = await res.text();
        try { return text ? JSON.parse(text) : {}; } catch { return { raw: text }; }
    }

    window.API = {
        async apiGet(url) {
            const res = await fetch(url, { credentials: 'same-origin' });
            return handle(res, 'GET', url);
        },
        async apiPostJSON(url, body) {
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken; // CSRF 보호
            const res = await fetch(url, {
                method: 'POST',
                headers,
                credentials: 'same-origin',
                body: JSON.stringify(body),
            });
            return handle(res, 'POST', url);
        }
    };
})();
