'use client'

import { useEffect, useState } from "react";
import { getProfileApi } from "../../lib/profile";

export default function useProfile() {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const res = await getProfileApi();
                if (res.success) {
                    setProfile(res.data); 
                } else {
                    setError(res.message);
                }
            } catch (err) {
                setError(err.message || "프로필을 불러오지 못했습니다.");
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, []);

    return { profile, loading, error };
}