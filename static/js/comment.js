// =======================
// 댓글 등록
// =======================
function registerComment(boardId) {

    const content = document.getElementById("commentContent").value.trim();

    if (content.length === 0) {
        alert("내용을 입력해주세요.");
        return;
    }

    const data = {
        boardId: boardId,
        content: content
    };

    fetch("/comments", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (!res.ok) {
                throw new Error("댓글 등록 실패");
            }
            return res.json();
        })
        .then(result => {
            alert("댓글이 등록되었습니다.");
            document.getElementById("commentContent").value = "";
            loadComments(boardId, 1);  // 1페이지 다시 불러오기
        })
        .catch(err => {
            console.error(err);
            alert("로그인이 필요합니다.");
        });
}


// =======================
// 댓글 목록 조회 (페이징)
// =======================
function loadComments(boardId, page) {

    fetch(`/comments/list/${boardId}?page=${page}`)
        .then(res => res.json())
        .then(data => {

            const commentList = document.getElementById("commentList");
            commentList.innerHTML = "";

            if (!data.dtoList || data.dtoList.length === 0) {
                commentList.innerHTML = "<p>등록된 댓글이 없습니다.</p>";
                return;
            }

            data.dtoList.forEach(comment => {

                const item = `
                    <div class="comment-item">
                        <strong>${comment.nickname}</strong>
                        <p>${comment.content}</p>
                        <small>${comment.regDate}</small>
                    </div>
                `;

                commentList.innerHTML += item;
            });
        });
}