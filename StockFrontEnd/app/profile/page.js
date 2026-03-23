import ProfileForm from "../../features/Profile/ProfileForm";
import { getProfileApi } from "../../lib/profile";

export default async function Profile() {
    const res = await getProfileApi();
    
    if (!res.success) {
        throw new Error(res.message); 
    }

    return (
        <div>
            <ProfileForm profile={res.data} />
        </div>
    );
}