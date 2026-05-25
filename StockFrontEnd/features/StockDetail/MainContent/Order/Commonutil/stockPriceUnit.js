export function getStockPriceUnit(price) {
    if (price < 2000) return 1
    if (price < 5000) return 5
    if (price < 10000) return 10
    if (price < 50000) return 50
    if (price < 100000) return 100
    if (price < 500000) return 500
    return 1000
}