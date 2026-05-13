// 函数需求：验证输入的邮箱字符串是否以 .edu.cn 结尾
  
// 要求：1. 使用正则表达式；2. 忽略大小写；3. 返回布尔值
export const validateEduEmail = (email) => {
  const regex = /^[^\s@]+@[^\s@]+\.edu\.cn$/i
  return regex.test(email)
}
// 为上述验证函数编写 3 个测试案例：1. 标准 edu 邮箱；2. 非 edu 邮箱；3. 格式错误的字符串
export const testValidateEduEmail = () => {
  console.log("测试 1: user@university.edu.cn =>", validateEduEmail('user@university.edu.cn'), "(期望 true)");
  console.log("测试 2: user@gmail.com =>", validateEduEmail('user@gmail.com'), "(期望 false)");
  console.log("测试 3: invalid-email =>", validateEduEmail('invalid-email'), "(期望 false)");
}
// 编写一个函数校验邮箱是否以 @fosu.edu.cn 结尾，并使用正则表达式
// 要求：1. 校验本地部分格式；2. 结尾固定为 fosu.edu.cn；3. 忽略大小写；4. 返回布尔值
export const validateFosuEmail = (email) => {
  const regex = /^[A-Za-z0-9._%+-]+@fosu\.edu\.cn$/i
  return regex.test(email)
}