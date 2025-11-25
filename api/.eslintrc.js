module.exports = {
  env: {
    node: true,
    es2021: true,
  },
  extends: ['airbnb-base', 'plugin:prettier/recommended'],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  rules: {
    'no-console': 'off',
    'prettier/prettier': 'error',
    camelcase: 'off', // 데이터베이스 컬럼명이 snake_case이므로 허용
    'consistent-return': 'off', // async 함수에서 명시적 반환 불필요
    'no-param-reassign': ['error', { props: false }], // 객체 속성 변경 허용
    'no-await-in-loop': 'off', // 초기화 스크립트에서 필요
    'no-restricted-syntax': 'off', // for...of 루프 허용
    'no-unused-vars': ['error', { argsIgnorePattern: '^_' }], // _로 시작하는 매개변수 무시
    'import/no-extraneous-dependencies': [
      'error',
      {
        devDependencies: false,
        optionalDependencies: false,
        peerDependencies: false,
      },
    ],
  },
};
