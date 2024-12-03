document.addEventListener('DOMContentLoaded', function() {
    const uploadForm = document.getElementById('uploadForm');
    const audioFile = document.getElementById('audioFile');
    const inputText = document.getElementById('inputText');
    const generateBtn = document.getElementById('generateBtn');
    const resultSection = document.querySelector('.result-section');
    const resultAudio = document.getElementById('resultAudio');
    const downloadBtn = document.getElementById('downloadBtn');

    generateBtn.addEventListener('click', async function() {
        if (!audioFile.files[0] || !inputText.value.trim()) {
            alert('请上传音频文件并输入文本！');
            return;
        }

        const formData = new FormData();
        formData.append('audio', audioFile.files[0]);
        formData.append('text', inputText.value.trim());

        try {
            generateBtn.disabled = true;
            generateBtn.textContent = '生成中...';

            const response = await fetch('/generate', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = URL.createObjectURL(blob);
                resultAudio.src = url;
                resultSection.style.display = 'block';
                
                downloadBtn.onclick = () => {
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = 'generated_audio.wav';
                    a.click();
                };
            } else {
                throw new Error('生成失败');
            }
        } catch (error) {
            alert('发生错误：' + error.message);
        } finally {
            generateBtn.disabled = false;
            generateBtn.textContent = '生成语音';
        }
    });
}); 