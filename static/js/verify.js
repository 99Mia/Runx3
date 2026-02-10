(function () {
    const $ = (id) => document.getElementById(id);

    // 🔐 CSRF 메타 태그에서 토큰/헤더 이름 읽어오기
    const csrfTokenMeta  = document.getElementById('_csrf');
    const csrfHeaderMeta = document.getElementById('_csrf_header');

    // ✅ JSON POST 공통 함수
    async function apiPostJSON(url, body) {

        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        // 🔐 CSRF 헤더가 있으면 같이 전송
        if (csrfTokenMeta && csrfHeaderMeta) {
            headers[csrfHeaderMeta.content] = csrfTokenMeta.content;
        }

        const res = await fetch(url, {
            method: 'POST',
            headers,
            credentials: 'same-origin',
            body: JSON.stringify(body)
        });

        const ct = res.headers.get('content-type') || '';

        // ❗ 서버가 HTML(에러 페이지, 로그인 페이지)을 돌려줄 때
        if (!ct.includes('application/json')) {
            // 더 이상 HTML 소스 전체를 안 뿌리고, 깔끔한 메시지만 던지기
            throw new Error(`서버 오류(${res.status})`);
        }

        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.message || `서버 오류(${res.status})`);
        }

        return data;
    }

    function init() {
        const email = $('email');
        const code  = $('code');
        const msg   = $('msg');
        const btnVerify = $('btnVerify');
        const btnResend = $('btnResend');

        // 🔁 인증 성공 후 이동할 기본 URL (원하는 가입완료 화면 URL로 바꿔줘)
        const SUCCESS_REDIRECT = '/main';      // 예: '/auth/signup-complete' 같은 걸로 변경 가능

        const show = (text, ok = false) => {
            msg.textContent = text;
            msg.className = 'msg ' + (ok ? 'ok' : 'err');
        };

        // ✅ 인증 버튼
        btnVerify.addEventListener('click', async (e) => {
            e.preventDefault();
            const em = (email.value || '').trim().toLowerCase();
            const cd = (code.value || '').trim();

            if (!em) return show('이메일을 입력해 주세요.');
            if (!/^\d{6}$/.test(cd)) return show('6자리 숫자 코드를 입력해 주세요.');

            btnVerify.disabled = true;
            try {
                const res = await apiPostJSON('/api/auth/verify-email', { email: em, code: cd });
                show(res.message || '인증되었습니다.', true);

                // 🔁 서버에서 redirect 값을 주더라도 무시하고
                //    항상 우리가 정한 가입완료 화면으로 이동
                setTimeout(() => {
                    location.href = SUCCESS_REDIRECT;
                }, 800);

            } catch (err) {
                show(err.message || '인증에 실패했습니다.');
            } finally {
                btnVerify.disabled = false;
            }
        });

        // ✅ 코드 재전송 버튼
        btnResend.addEventListener('click', async () => {
            const em = (email.value || '').trim().toLowerCase();
            if (!em) return show('이메일을 입력해 주세요.');

            btnResend.disabled = true;
            try {
                const res = await apiPostJSON('/api/auth/resend-email-code', { email: em });
                show(res.message || '인증코드를 재발송했습니다.', true);
            } catch (err) {
                show(err.message || '재발송에 실패했습니다.');
            } finally {
                setTimeout(() => btnResend.disabled = false, 1500);
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
