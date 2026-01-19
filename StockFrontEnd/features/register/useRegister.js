import { RegisterSumbitApi } from "../../lib/user"
import { useState } from "react";

export default function useRegister() {
  const [formData, setFormData] = useState({ id:"", username: "", password: "" });

  const submitHandler = async (e) => {
    e.preventDefault();
    const res = await RegisterSumbitApi(formData);
    console.log(await res.text());
  };

  return {
    formData,
    setFormData,
    submitHandler
  };
}