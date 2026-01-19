'use client'

import { useState } from "react";
import useRegister from "./useRegister";

export default function RegisterForm() {
    const { formData, setFormData, submitHandler } = useRegister();

    return (
        <div style={{ padding: "20px" }}>
            <form onSubmit={submitHandler}>
                   <input
                    type="text"
                    placeholder="id"
                    value={formData.id}
                    onChange={e => setFormData({ ...formData, id: e.target.value })}
                />
                <input
                    type="text"
                    placeholder="Username"
                    value={formData.username}
                    onChange={e => setFormData({ ...formData, username: e.target.value })}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={formData.password}
                    onChange={e => setFormData({ ...formData, password: e.target.value })}
                />
                <button type="submit">회원가입</button>
            </form>
        </div>
    );
}
