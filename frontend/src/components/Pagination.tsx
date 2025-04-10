type PaginationProps = {
    currentPage: number
    totalPages: number
    onPageChange: (page: number) => void
}

export function Pagination({ currentPage, totalPages, onPageChange }: PaginationProps) {
    return (
        <div style={{ marginTop: "1rem", textAlign: "center" }}>
            {Array.from({ length: totalPages }, (_, i) => i).map((page) => (
                <button
                    key={page}
                    onClick={() => onPageChange(page)}
                    style={{
                        margin: "0 4px",
                        padding: "0.4rem 0.8rem",
                        backgroundColor: page === currentPage ? "#007bff" : "#e4ecf7",
                        color: page === currentPage ? "white" : "#333",
                        border: "none",
                        borderRadius: "4px",
                        cursor: "pointer"
                    }}
                >
                    {page + 1}
                </button>
            ))}
        </div>
    )
}
