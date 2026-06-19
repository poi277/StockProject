export function getDiffColor(value) {
    if (value === 0) return "var(--wts-adaptive-grey600)";
    return value > 0 ? "var(--wts-adaptive-red500)" : "var(--wts-adaptive-blue500)";
}
