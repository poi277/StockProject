import { useRouter } from 'next/navigation';
import { useAuth } from "../../context/AuthContext";

export default function useHeader()
{
    const router = useRouter();
    const {user,logout} = useAuth();

    const handleLogout = async () => {
        await logout();
        router.push('/');
    };
    const handleLogin = async () => {
        router.push('/login');
    }

    return {handleLogout,handleLogin,user}
}