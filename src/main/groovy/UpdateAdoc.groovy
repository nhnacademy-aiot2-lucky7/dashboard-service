// 스니펫이 저장된 디렉토리와 index.adoc 파일 경로 설정
def snippetDir = new File("target/generated-snippets")
def indexFile = new File("src/docs/asciidoc/index.adoc")

// 기존 index.adoc 파일 읽기
def lines = indexFile.readLines()

// 자동 생성된 섹션을 알리는 주석 추가
lines << ""
lines << "// === 자동 생성된 API 문서 ==="
lines << ""

// 디렉토리 내 스니펫 폴더 순회
snippetDir.eachDir { dir ->
    def snippetName = dir.name

    // 섹션 제목 추가 (ex: == user-login)
    lines << "== ${snippetName.replace('-', ' ').capitalize()}"
    lines << ""

    // 상대 경로로 수정된 include
    lines << "[source,http]"
    lines << "include::{snippets}/${snippetName}/http-request.adoc[]"
    lines << ""

    lines << "[source,http]"
    lines << "include::{snippets}/${snippetName}/http-response.adoc[]"
}

// 정리한 내용을 index.adoc에 다시 저장
indexFile.text = lines.join("\n")
