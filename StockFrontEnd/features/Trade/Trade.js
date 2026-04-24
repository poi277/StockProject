// 'use client';

// import { useState } from 'react';
// import { tradeApi } from '../../lib/trade';

// export default function Trade(selectedPrice,stockCode) {
//   const [tradeType, setTradeType] = useState('BUY');
//   const [quantity, setQuantity] = useState(1);

//   const [message, setMessage] = useState('');
//   const [error, setError] = useState('');

//   const handleSubmit = async (e) => {
//     e.preventDefault();
//     setMessage('');
//     setError('');

//     if (quantity <= 0) {
//       setError('수량을 올바르게 입력해주세요.');
//       return;
//     }
//     try {
//       const res = await tradeApi(tradeType,stockCode,quantity,selectedPrice)
//       if (!res.success) {
//         setError(res.message);
//         return;
//       }
//       setMessage(res.message);
//     } catch (err) {
//       console.error(err);
//       setError('거래 요청 중 오류가 발생했습니다.');
//     }
//   };
//   return {
//     tradeType,
//     quantity,
//     message,
//     error,
//     setTradeType,
//     setQuantity,
//     handleSubmit,
//   };
// }
