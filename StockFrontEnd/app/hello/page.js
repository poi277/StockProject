'use client'

import { useEffect } from 'react'; // import 추가!
import { getHello } from "../../lib/route";

export default function HelloPage() { 
  useEffect(() => {
    const fetchData = async () => {
      const res = await getHello(); 
      const data = await res.json()
      console.log(data);
    };
    
    fetchData();
  }, []);

  return (
    <div>
      hello
    </div>
  );
}