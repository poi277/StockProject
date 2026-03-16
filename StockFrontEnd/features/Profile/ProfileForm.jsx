'use client'

import useProfile from "./useProfile";

export default function ProfileForm() {
    const { profile, loading, error } = useProfile();

    if (loading) return <div>로딩 중...</div>;
    if (error)   return <div style={{ color: 'red' }}>{error}</div>;

    return (
        <div style={{ padding: '20px', maxWidth: '400px', margin: '0 auto' }}>
            <h2>프로필</h2>
            <div style={{
                border: '1px solid #ddd',
                borderRadius: '8px',
                padding: '20px',
                backgroundColor: '#fff'
            }}>
                <p><strong>아이디:</strong> {profile?.id}</p>
            </div>
        </div>
    );
}