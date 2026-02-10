package org.run.runx3.psh.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.run.runx3.common.config.CustomUserDetails;
import org.run.runx3.psh.dto.BoardDTO;
import org.run.runx3.psh.dto.BoardImageDTO;
import org.run.runx3.psh.dto.page.PageRequestDTO;
import org.run.runx3.psh.dto.page.PageResponseDTO;
import org.run.runx3.psh.dto.toggle.*;
import org.run.runx3.psh.dto.upload.UploadFileDTO;
import org.run.runx3.psh.repository.BoardRepository;
import org.run.runx3.psh.service.board.BoardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/board")
@Log4j2
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final BoardRepository boardRepository;

    // ✅ application.properties에 정의된 경로 키로 변경
    @Value("${file.upload-dir}")
    private String uploadPath;

    // =======================
// 게시글 목록
// =======================
    @GetMapping("/list")
    public String list(@ModelAttribute PageRequestDTO pageRequestDTO,
                       Model model,
                       Principal principal) {

        PageResponseDTO<BoardDTO> responseDTO = boardService.getList(pageRequestDTO);

        // 기본값 (비로그인 유저)
        Long userId = 5L;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {

            Object principalObj = auth.getPrincipal();

            try {
                // ⭕ 일반 로그인(CustomUserDetails)
                if (principalObj instanceof CustomUserDetails user) {
                    userId = user.getId();
                }
                // ⭕ 구글(OIDC) 로그인 사용자 - DefaultOidcUser
                else if (principalObj instanceof org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser oidcUser) {
                    String email = oidcUser.getEmail();
                    userId = boardService.findUserIdByEmail(email);  // 이메일로 DB 조회
                }
                // ⭕ 네이버/카카오 OAuth2 로그인 사용자 - DefaultOAuth2User
                else if (principalObj instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User oauth2User) {
                    String email = oauth2User.getAttribute("email");
                    userId = boardService.findUserIdByEmail(email);
                }
            } catch (Exception e) {
                log.error("User principal parse error: " + e.getMessage());
            }
        }

        // 각 게시글별 좋아요/북마크/신고 여부 반영
        for (BoardDTO board : responseDTO.getDtoList()) {
            board.setLiked(boardService.isLikedByUser(board.getBoardId(), userId));
            board.setBookmarked(boardService.isBookmarkedByUser(board.getBoardId(), userId));
            board.setReported(boardService.isReportedByUser(board.getBoardId(), userId));
        }

        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        return "board/list";
    }


    // =======================
    // 등록 폼
    // =======================
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("boardDTO", new BoardDTO());
        return "board/register";
    }

    // =======================
    // 게시글 등록 처리
    // =======================
    @PostMapping("/register")
    public String registerPost(@ModelAttribute BoardDTO boardDTO,
                               UploadFileDTO uploadFileDTO,
                               Principal principal) {

        // 1️⃣ 파일 업로드
        List<BoardImageDTO> imageDTOS = new ArrayList<>();
        if (uploadFileDTO.getFiles() != null &&
                !uploadFileDTO.getFiles().isEmpty() &&
                !uploadFileDTO.getFiles().get(0).getOriginalFilename().isEmpty()) {
            imageDTOS = uploadFiles(uploadFileDTO);
        }

        boardDTO.setBoardImageDTOS(imageDTOS);

        if (!imageDTOS.isEmpty()) {
            BoardImageDTO firstImg = imageDTOS.get(0);
            boardDTO.setImage(firstImg.getSavePath());
        }

        // 2️⃣ 사용자 정보 (로그인 전 임시 처리)
        if (principal != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

            boardDTO.setUserId(user.getId());
            boardDTO.setUsername(user.getUsername());
        }

        Long boardId = boardService.insertBoard(boardDTO);
        return "redirect:/board/list";
    }



    @GetMapping("/community-list")
    public String communityList() {
        return "board/list";   // board/list.html 로 이동
    }

    // =======================
    // 게시글 읽기
    // =======================
    @GetMapping("/read")
    public String readBoard(@RequestParam("boardId") Long boardId,
                            PageRequestDTO pageRequestDTO,
                            Model model) {
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("boardDTO", boardService.findBoardById(boardId, 1));
        return "board/read";
    }

    // =======================
    // 수정 폼
    // =======================
    @GetMapping("/modify")
    public String modifyBoardForm(@RequestParam("boardId") Long boardId,
                                  PageRequestDTO pageRequestDTO,
                                  Model model) {
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        model.addAttribute("boardDTO", boardService.findBoardById(boardId, 2));
        return "board/modify";
    }

    // =======================
    // 수정 처리
    // =======================
    @PostMapping("/modify")
    public String modifyBoard(@ModelAttribute BoardDTO boardDTO,
                              @ModelAttribute UploadFileDTO uploadFileDTO,
                              RedirectAttributes redirectAttributes) {

        log.info("modifyBoard: {}", boardDTO);

        BoardDTO existingBoard = boardService.findBoardById(boardDTO.getBoardId(), 2);

        List<BoardImageDTO> newImageDTOS = existingBoard.getBoardImageDTOS();
        if (uploadFileDTO.getFiles() != null &&
                !uploadFileDTO.getFiles().get(0).getOriginalFilename().equals("")) {

            removeFile(existingBoard.getBoardImageDTOS());
            newImageDTOS = uploadFiles(uploadFileDTO);
        }

        boardDTO.setBoardImageDTOS(newImageDTOS);
        boardDTO.setUserId(existingBoard.getUserId());
        boardDTO.setUsername(existingBoard.getUsername());
        boardDTO.setRegDate(existingBoard.getRegDate());

        boardService.updateBoard(boardDTO);

        redirectAttributes.addAttribute("boardId", boardDTO.getBoardId());
        redirectAttributes.addAttribute("mode", 1);

        return "redirect:/board/read";
    }

    // =======================
    // 게시글 삭제
    // =======================
    @GetMapping("/remove")
    public String removeBoard(@RequestParam("boardId") Long boardId,
                              RedirectAttributes redirectAttributes) {

        log.info("removeBoard boardId={}", boardId);

        BoardDTO boardDTO = boardService.findBoardById(boardId, 2);
        if (boardDTO == null) {
            redirectAttributes.addFlashAttribute("msg", "삭제할 게시글이 존재하지 않습니다.");
            return "redirect:/board/list";
        }

        List<BoardImageDTO> imageDTOS = boardDTO.getBoardImageDTOS();
        if (imageDTOS != null && !imageDTOS.isEmpty()) {
            removeFile(imageDTOS);
        }

        boardService.deleteBoard(boardId);
        redirectAttributes.addFlashAttribute("msg", "게시글이 삭제되었습니다.");

        return "redirect:/board/list";
    }

    // =======================
    // 좋아요 / 북마크 / 신고
    // =======================
    @PostMapping("/like/toggle")
    @ResponseBody
    public LikeResponseDTO toggleLike(@RequestBody LikeRequestDTO request) {
        boolean liked = boardService.toggleLike(request.getBoardId(), request.getUserId());
        int likeCount = boardService.getLikeCount(request.getBoardId());
        return new LikeResponseDTO(liked, likeCount);
    }

    @PostMapping("/bookmark/toggle")
    @ResponseBody
    public BookmarkResponseDTO toggleBookmark(@RequestBody BookmarkRequestDTO request) {
        boolean bookmarked = boardService.toggleBookmark(request.getBoardId(), request.getUserId());
        int bookmarkCount = boardService.getBookmarkCount(request.getBoardId());
        return new BookmarkResponseDTO(bookmarked, bookmarkCount);
    }

    @PostMapping("/report")
    @ResponseBody
    public ReportResponseDTO reportBoard(@RequestBody ReportRequestDTO request) {
        boolean reported = boardService.reportBoard(request.getBoardId(), request.getUserId(), request.getReason());
        int reportCount = boardService.getReportCount(request.getBoardId());
        return new ReportResponseDTO(reported, reportCount);
    }

    // =======================
    // 이미지 보기
    // =======================
    @GetMapping("/upload/view/{filename}")
    @ResponseBody
    public Resource viewImage(@PathVariable String filename) throws IOException {
        if (filename == null || filename.isEmpty()) return null;

        Path filePath = Paths.get(uploadPath, filename);
        if (!Files.exists(filePath)) return null;

        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
                lowerName.endsWith(".png") || lowerName.endsWith(".gif")) {
            return new FileSystemResource(filePath.toFile());
        } else {
            return new ClassPathResource("static/images/file.jpg");
        }
    }

    // =======================
    // 파일 업로드
    // =======================
    private List<BoardImageDTO> uploadFiles(UploadFileDTO uploadFileDTO) {
        List<BoardImageDTO> list = new ArrayList<>();

        if (uploadFileDTO.getFiles() != null) {
            uploadFileDTO.getFiles().forEach(multiFile -> {
                String originalFileName = multiFile.getOriginalFilename();
                if (originalFileName == null || originalFileName.isBlank()) return;

                log.info("originalFileName: {}", originalFileName);
                String uuid = UUID.randomUUID().toString();
                Path savePath = Paths.get(uploadPath, uuid + "_" + originalFileName);
                boolean image = false;

                try {
                    multiFile.transferTo(savePath);

                    // ✅ null 체크 추가
                    String contentType = Files.probeContentType(savePath);
                    if (contentType != null && contentType.startsWith("image")) {
                        image = true;
                        String thumbnailName = "s_" + uuid + "_" + originalFileName;
                        File thumbnail = new File(uploadPath, thumbnailName);
                        Thumbnailator.createThumbnail(savePath.toFile(), thumbnail, 200, 200);
                    }

                } catch (Exception e) {
                    log.error("File upload error: {}", e.getMessage());
                }

                BoardImageDTO boardImageDTO = BoardImageDTO.builder()
                        .uuid(uuid)
                        .filename(originalFileName)
                        .image(image)
                        .savePath(uuid + "_" + originalFileName)
                        .thumbnail("s_" + uuid + "_" + originalFileName)
                        .build();

                list.add(boardImageDTO);
            });
        }
        return list;
    }

    // =======================
    // 파일 삭제
    // =======================
    public void removeFile(List<BoardImageDTO> imageDTOS) {
        for (BoardImageDTO boardImageDTO : imageDTOS) {
            String filename = boardImageDTO.getUuid() + "_" + boardImageDTO.getFilename();
            Resource resource = new FileSystemResource(uploadPath + File.separator + filename);

            try {
                if (resource.exists() && resource.isFile()) {
                    resource.getFile().delete();

                    String contentType = Files.probeContentType(resource.getFile().toPath());
                    if (contentType != null && contentType.startsWith("image")) {
                        String thumbName = "s_" + boardImageDTO.getUuid() + "_" + boardImageDTO.getFilename();
                        File thumbnail = new File(uploadPath + File.separator + thumbName);
                        if (thumbnail.exists()) thumbnail.delete();
                    }
                }
            } catch (IOException e) {
                log.error("File remove error: {}", e.getMessage());
            }
        }
    }
}
