// /js/challenges.js

document.addEventListener("DOMContentLoaded", () => {

    let currentPage    = 0;
    let currentStatus  = "";
    let currentKeyword = "";

    const cardGrid    = document.getElementById("cardGrid");
    const pagination  = document.getElementById("pagination");
    const tabs        = document.querySelectorAll(".rw-tabs .tab");
    const searchInput = document.getElementById("q");
    const searchForm  = document.getElementById("searchForm");

    // CSRF
    const csrfTokenMeta  = document.getElementById("_csrf");
    const csrfHeaderMeta = document.getElementById("_csrf_header");
    const csrfToken  = csrfTokenMeta  ? csrfTokenMeta.getAttribute("content") : null;
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : null;

    /* --------------------------------------------------
     * 공통 POST (CSRF 포함)
     * -------------------------------------------------- */
    async function postJSON(url, body = {}) {
        const headers = {
            "Content-Type": "application/json",
            "Accept": "application/json"
        };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        const res = await fetch(url, {
            method: "POST",
            headers,
            body: JSON.stringify(body),
            credentials: "same-origin"
        });

        const ct   = res.headers.get("content-type") || "";
        const json = ct.includes("application/json");
        const data = json ? await res.json() : await res.text();

        if (!res.ok) {
            console.error("POST 오류:", data);
            throw new Error("요청 실패");
        }
        return data;
    }

    /* --------------------------------------------------
     * 상태 뱃지
     * -------------------------------------------------- */
    /* --------------------------------------------------
     * 상태 뱃지 (날짜 기준으로 계산)
     * -------------------------------------------------- */
    function getStatusBadge(c) {
        const now = new Date();

        const start = c.startDate ? new Date(c.startDate) : null;
        const end   = c.endDate   ? new Date(c.endDate)   : null;

        // 날짜 둘 다 있는 경우
        if (start && end) {

            // 1) 이미 끝난 대회
            if (now > end) {
                return { text: "종료", cls: "rw-badge--ended" };
            }

            // 2) 아직 시작 전 (예정 / D-Day)
            if (now < start) {
                const diffMs   = start.getTime() - now.getTime();
                const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24)); // 내림 대신 올림

                // 시작 7일 이내면 D-Day
                if (diffDays <= 7) {
                    return { text: `D-${diffDays}`, cls: "rw-badge--dday" };
                }

                return { text: "예정", cls: "rw-badge--upcoming" };
            }

            // 3) 오늘 날짜가 start~end 사이면 진행중
            return { text: "진행중", cls: "rw-badge--ongoing" };
        }

        // 날짜가 없으면 서버 status 값에 한 번 더 기대보고,
        // 그것도 없으면 그냥 예정으로 표시
        if (c.status) {
            switch (c.status) {
                case "ONGOING":
                    return { text: "진행중", cls: "rw-badge--ongoing" };
                case "ENDED":
                    return { text: "종료", cls: "rw-badge--ended" };
                case "UPCOMING":
                default:
                    return { text: "예정", cls: "rw-badge--upcoming" };
            }
        }

        return { text: "예정", cls: "rw-badge--upcoming" };
    }


    /* --------------------------------------------------
     * 대회 목록 로드
     * -------------------------------------------------- */
    async function loadChallenges(page = 0) {

        const url =
            `/api/challenges?page=${page}&size=12`
            + `&status=${currentStatus}`
            + `&q=${currentKeyword}`;

        cardGrid.setAttribute("aria-busy", "true");

        const res  = await fetch(url);
        const data = await res.json();

        cardGrid.innerHTML = "";

        data.content.forEach(c => {
            const badge = getStatusBadge(c);

            // DTO 필드에 맞게 카운트 사용
            const likeCount     = (c.likeCnt ?? c.likeCount ?? 0);
            const bookmarkCount = (c.bookmarkCnt ?? c.bookmarkCount ?? 0);

            // 리스트에서 찜/좋아요 여부 플래그가 내려올 수도 있으니 넉넉하게 처리
            const isLiked      = !!(c.liked ?? c.likeStatus ?? c.isLiked);
            const isBookmarked = !!(c.bookmarked ?? c.bookmarkStatus ?? c.isBookmarked);

            // ✅ 신청/보기 링크: registrationLink → viewLink → 내부 상세
            const externalUrl = c.registrationLink || c.viewLink || "";
            const detailHref  = externalUrl || `/challenges/${c.challengeId}`;
            const targetAttr  = externalUrl
                ? ' target="_blank" rel="noopener noreferrer"'
                : '';

            // 이미지: imageUrl 우선, 없으면 posterUrl, 둘 다 없으면 no-image
            const imgSrc = c.imageUrl || c.posterUrl || '/assets/no-image.png';

            cardGrid.innerHTML += `
                <article class="rw-card">

                    <!-- 상단: 상태 뱃지 + 좋아요/찜 -->
                    <div class="rw-card__header">
                        <span class="rw-badge ${badge.cls}">
                            ${badge.text}
                        </span>

                        <div class="rw-card__meta">
                            <button type="button"
                                    class="rw-meta__item js-like ${isLiked ? "is-active" : ""}"
                                    data-id="${c.challengeId}">
                                <i class="${isLiked ? "ri-heart-3-fill" : "ri-heart-3-line"}"></i>
                                <span>${likeCount}</span>
                            </button>
                            <button type="button"
                                    class="rw-meta__item js-bookmark ${isBookmarked ? "is-active" : ""}"
                                    data-id="${c.challengeId}">
                                <i class="${isBookmarked ? "ri-bookmark-fill" : "ri-bookmark-line"}"></i>
                                <span>${bookmarkCount}</span>
                            </button>
                        </div>
                    </div>

                    <!-- 포스터: 관리자 URL 있으면 새창, 없으면 내부 상세 -->
                    <a href="${detailHref}" class="rw-card__poster"${targetAttr}>
                        <img src="${imgSrc}"
                             alt="${c.title}"
                             class="rw-card__img">
                    </a>

                    <!-- 텍스트 정보 -->
                    <div class="rw-card__body">
                        <h3 class="rw-card__title">${c.title}</h3>

                        <p class="rw-card__location">
                            ${c.location || '장소 미정'}
                        </p>

                        <p class="rw-card__date">
                            ${(c.startDate || "").substring(0, 10)}
                            ~
                            ${(c.endDate || "").substring(0, 10)}
                        </p>

                        <p class="rw-card__fee">
                            참가비: ${c.fee ? c.fee + '원' : '무료'}
                        </p>
                    </div>

                    <!-- 하단 버튼: 동일 링크 -->
                    <div class="rw-card__footer">
                        <a href="${detailHref}"
                           class="rw-btn rw-btn--primary rw-btn--full"${targetAttr}>
                            상세보기
                        </a>
                    </div>
                </article>
            `;
        });

        cardGrid.setAttribute("aria-busy", "false");

        bindMetaEvents();
        renderPagination(data);
    }

    /* --------------------------------------------------
     * 좋아요 / 찜 버튼 이벤트
     * -------------------------------------------------- */
    function bindMetaEvents() {
        // 좋아요
        cardGrid.querySelectorAll(".js-like").forEach(btn => {
            btn.addEventListener("click", async (e) => {
                e.stopPropagation();
                const id = btn.dataset.id;

                try {
                    const data = await postJSON(`/api/challenges/${id}/like`);
                    const span = btn.querySelector("span");
                    const icon = btn.querySelector("i");

                    const newCount =
                        data.likeCnt ?? data.likeCount ??
                        data.count   ?? null;

                    if (span && typeof newCount === "number") {
                        span.textContent = newCount;
                    }

                    const liked =
                        (data.liked ?? data.likeStatus ?? data.isLiked) ?? null;

                    if (liked !== null) {
                        btn.classList.toggle("is-active", !!liked);
                        if (icon) {
                            icon.className = liked ? "ri-heart-3-fill" : "ri-heart-3-line";
                        }
                    }
                } catch (err) {
                    console.error(err);
                    alert("좋아요 처리 중 오류가 발생했습니다.");
                }
            });
        });

        // 찜
        cardGrid.querySelectorAll(".js-bookmark").forEach(btn => {
            btn.addEventListener("click", async (e) => {
                e.stopPropagation();
                const id = btn.dataset.id;

                try {
                    const data = await postJSON(`/api/challenges/${id}/bookmark`);
                    const span = btn.querySelector("span");
                    const icon = btn.querySelector("i");

                    const newCount =
                        data.bookmarkCnt ?? data.bookmarkCount ??
                        data.count       ?? null;

                    if (span && typeof newCount === "number") {
                        span.textContent = newCount;
                    }

                    const bookmarked =
                        (data.bookmarked ?? data.bookmarkStatus ?? data.isBookmarked) ?? null;

                    if (bookmarked !== null) {
                        btn.classList.toggle("is-active", !!bookmarked);
                        if (icon) {
                            icon.className = bookmarked
                                ? "ri-bookmark-fill"
                                : "ri-bookmark-line";
                        }
                    }
                } catch (err) {
                    console.error(err);
                    alert("찜 처리 중 오류가 발생했습니다.");
                }
            });
        });
    }

    /* --------------------------------------------------
     * 페이지네이션
     * -------------------------------------------------- */
    function renderPagination(data) {
        pagination.innerHTML = "";

        const totalPages = data.totalPages;
        if (totalPages <= 1) return;

        let html = "";

        if (!data.first) {
            html += `<button class="page-btn" data-page="${data.number - 1}">‹</button>`;
        }

        for (let i = 0; i < totalPages; i++) {
            html += `
                <button class="page-btn ${i === data.number ? "active" : ""}"
                        data-page="${i}">
                    ${i + 1}
                </button>
            `;
        }

        if (!data.last) {
            html += `<button class="page-btn" data-page="${data.number + 1}">›</button>`;
        }

        pagination.innerHTML = html;

        document.querySelectorAll(".page-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                const page = parseInt(btn.dataset.page, 10);
                currentPage = page;
                loadChallenges(page);
            });
        });
    }

    /* --------------------------------------------------
     * 상태 탭
     * -------------------------------------------------- */
    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");

            currentStatus = tab.dataset.status || "";
            currentPage   = 0;

            loadChallenges(0);
        });
    });

    /* --------------------------------------------------
     * 검색
     * -------------------------------------------------- */
    if (searchForm) {
        searchForm.addEventListener("submit", (e) => {
            e.preventDefault();
            currentKeyword = (searchInput?.value || "").trim();
            currentPage    = 0;
            loadChallenges(0);
        });
    }

    /* --------------------------------------------------
     * 최초 로드
     * -------------------------------------------------- */
    loadChallenges(0);
});
