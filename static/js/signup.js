(function () {
    // ---- Fallback API (window.API가 없을 경우 대비) -------------------------
    if (!window.API) window.API = {};
    if (!API.apiGet) {
        API.apiGet = async (url) => {
            const res = await fetch(url, {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin',
            });
            const ct = res.headers.get('content-type') || '';
            if (!res.ok) {
                // HTML로 리다이렉트/오류가 오면 본문을 그대로 띄워도 좋지만 간단히 처리
                const text = await res.text();
                throw new Error(`서버 오류(${res.status})`);
            }
            if (!ct.includes('application/json')) {
                // 로그인 페이지(HTML) 등으로 리다이렉트 된 경우
                const text = await res.text();
                throw new Error('서버 응답 형식이 예상과 달라요.');
            }
            return res.json();
        };
    }
    if (!API.apiPostJSON) {
        API.apiPostJSON = async (url, body) => {
            const res = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
                credentials: 'same-origin',
                body: JSON.stringify(body),
            });
            const ct = res.headers.get('content-type') || '';
            if (!res.ok) {
                // 가능하면 서버가 내려준 메시지를 보여주자
                let msg = `서버 오류(${res.status})`;
                try {
                    if (ct.includes('application/json')) {
                        const j = await res.json();
                        if (j.message) msg = j.message;
                    } else {
                        const t = await res.text();
                        if (t) msg = t.substring(0, 200);
                    }
                } catch (_) {}
                throw new Error(msg);
            }
            if (!ct.includes('application/json')) {
                // 회원가입 API는 JSON을 돌려주게 해놨으니 JSON 아니면 경고
                const text = await res.text();
                throw new Error('서버 응답 형식이 예상과 달라요.');
            }
            return res.json();
        };
    }
    // ------------------------------------------------------------------------

    function init() {
        console.log("[signup.js] init");
        const $ = (id) => document.getElementById(id);

        const form = $('signupForm');
        if (!form) { console.warn("signupForm not found"); return; }

        const email = $('email');
        const btnCheckEmail = $('btnCheckEmail');
        const emailMsg = $('emailMsg');

        const password = $('password');
        const confirmPw = $('confirmPassword');
        const passwordMsg = $('passwordMsg');

        const username = $('username');
        const nickname = $('nickname');
        const btnCheckNick = $('btnCheckNickname');
        const nicknameMsg = $('nicknameMsg');

        const phone = $('phone');
        const agreePrivacy = $('agreePrivacy');
        const agreeTerms = $('agreeTerms');
        const btnSignup = $('btnSignup');
        const errorArea = $('errorArea');

        let emailOK = false;
        let nickOK  = false;

        const validatePasswordRule = (pw) =>
            /[A-Za-z]/.test(pw) && /\d/.test(pw) &&
            /[!@#$%^&*()_+=\-]/.test(pw) && pw.length >= 8 && pw.length <= 32;

        const refreshSubmitState = () => {
            const baseValid = email.value && username.value && nickname.value &&
                validatePasswordRule(password.value) &&
                password.value === confirmPw.value &&
                agreePrivacy.checked && agreeTerms.checked;
            btnSignup.disabled = !(baseValid && emailOK && nickOK);
        };

        const updatePasswordMsg = () => {
            if (!password.value) { passwordMsg.textContent = ''; return; }
            if (!validatePasswordRule(password.value)) {
                passwordMsg.style.color = 'crimson';
                passwordMsg.textContent = '영문/숫자/특수문자 포함 8~32자';
            } else if (confirmPw.value && password.value !== confirmPw.value) {
                passwordMsg.style.color = 'crimson';
                passwordMsg.textContent = '비밀번호 확인이 일치하지 않습니다.';
            } else {
                passwordMsg.style.color = 'seagreen';
                passwordMsg.textContent = '사용 가능한 비밀번호입니다.';
            }
        };

        email.addEventListener('input', () => { emailOK = false; emailMsg.textContent = ''; refreshSubmitState(); });
        nickname.addEventListener('input', () => { nickOK = false; nicknameMsg.textContent = ''; refreshSubmitState(); });
        password.addEventListener('input', () => { updatePasswordMsg(); refreshSubmitState(); });
        confirmPw.addEventListener('input', () => { updatePasswordMsg(); refreshSubmitState(); });
        agreePrivacy.addEventListener('change', refreshSubmitState);
        agreeTerms.addEventListener('change',  refreshSubmitState);

        // 이메일 중복확인
        btnCheckEmail.addEventListener('click', async () => {
            try {
                emailMsg.textContent = '확인 중...'; emailMsg.style.color = '';
                const q = encodeURIComponent(email.value.trim().toLowerCase());
                if (!q) throw new Error('이메일을 입력해 주세요.');

                const data = await API.apiGet(`/api/auth/check-email?email=${q}`);
                // 서버 응답 포맷 유연 처리
                const available = typeof data.available === 'boolean' ? data.available
                    : typeof data.exists   === 'boolean' ? !data.exists
                        : data.status === 'AVAILABLE' ? true
                            : data.status === 'TAKEN'     ? false : null;
                if (available === null) throw new Error('서버 응답 형식이 예상과 달라요.');

                emailOK = available;
                emailMsg.style.color = available ? 'seagreen' : 'crimson';
                emailMsg.textContent = available ? (data.message || '사용 가능한 이메일입니다.')
                    : (data.message || '이미 사용 중인 이메일입니다.');
            } catch (e) {
                emailOK = false;
                emailMsg.style.color = 'crimson';
                emailMsg.textContent = e.message || '이메일 확인 중 오류가 발생했습니다.';
            } finally {
                refreshSubmitState();
            }
        });

        // 닉네임 중복확인
        btnCheckNick.addEventListener('click', async () => {
            try {
                nicknameMsg.textContent = '확인 중...'; nicknameMsg.style.color = '';
                const q = encodeURIComponent(nickname.value.trim());
                if (!q) throw new Error('닉네임을 입력해 주세요.');

                const data = await API.apiGet(`/api/auth/check-nickname?nickname=${q}`);
                const available = typeof data.available === 'boolean' ? data.available
                    : typeof data.exists   === 'boolean' ? !data.exists
                        : data.status === 'AVAILABLE' ? true
                            : data.status === 'TAKEN'     ? false : null;
                if (available === null) throw new Error('서버 응답 형식이 예상과 달라요.');

                nickOK = available;
                nicknameMsg.style.color = available ? 'seagreen' : 'crimson';
                nicknameMsg.textContent = available ? (data.message || '사용 가능한 닉네임입니다.')
                    : (data.message || '이미 사용 중인 닉네임입니다.');
            } catch (e) {
                nickOK = false;
                nicknameMsg.style.color = 'crimson';
                nicknameMsg.textContent = e.message || '닉네임 확인 중 오류가 발생했습니다.';
            } finally {
                refreshSubmitState();
            }
        });

        // 제출
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (btnSignup.disabled) return;

            const body = {
                email: email.value.trim().toLowerCase(),
                password: password.value,
                confirmPassword: confirmPw.value,
                username: username.value.trim(),
                nickname: nickname.value.trim(),
                phone: (phone.value || '').replace(/\D/g, ''),
                agreePrivacy: agreePrivacy.checked,
                agreeTerms:   agreeTerms.checked,
            };

            const orig = btnSignup.textContent;
            btnSignup.textContent = '가입 처리 중...';
            btnSignup.disabled = true;
            errorArea.textContent = '';

            try {
                await API.apiPostJSON('/api/auth/signup', body);

                // 이메일 인증 안내 → 인증 페이지로 이동 (페이지가 없으면 로그인으로 바꿔도 OK)
                alert('인증코드를 이메일로 보냈어요. 메일함에서 코드를 확인해 주세요.');
                const nextUrl = `/auth/verify?email=${encodeURIComponent(body.email)}`; // ← 페이지 없으면 '/auth/login'로 변경 가능
                window.location.href = nextUrl;

            } catch (e2) {
                errorArea.style.color = 'crimson';
                errorArea.textContent = e2.message || '가입에 실패했습니다.';
            } finally {
                btnSignup.textContent = orig;
                refreshSubmitState();
            }
        });

        refreshSubmitState();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})
();
