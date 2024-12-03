from flask import Flask, render_template, request, send_file
import os
from werkzeug.utils import secure_filename
import logging
import socket
import glob

# 配置日志显示更详细的信息
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# 获取项目根目录的绝对路径
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))

# 设置应用目录
APP_DIR = os.path.join(ROOT_DIR, 'app')
STATIC_DIR = os.path.join(APP_DIR, 'static')
TEMPLATES_DIR = os.path.join(APP_DIR, 'templates')
UPLOAD_DIR = os.path.join(APP_DIR, 'uploads')

# 创建应用实例
app = Flask(__name__,
           static_folder=STATIC_DIR,
           template_folder=TEMPLATES_DIR)

# 确保所有必要的目录都存在
for directory in [APP_DIR, STATIC_DIR, 
                 os.path.join(STATIC_DIR, 'css'),
                 os.path.join(STATIC_DIR, 'js'), 
                 TEMPLATES_DIR, 
                 UPLOAD_DIR]:
    os.makedirs(directory, exist_ok=True)
    logger.info(f"Directory exists or created: {directory}")

# 配置上传文件夹
app.config['UPLOAD_FOLDER'] = UPLOAD_DIR
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB max-limit

@app.before_request
def log_request_info():
    logger.debug('Headers: %s', request.headers)
    logger.debug('Body: %s', request.get_data())
    logger.debug('Project Root: %s', ROOT_DIR)
    logger.debug('Request Path: %s', request.path)
    logger.debug('Request Method: %s', request.method)

@app.route('/')
def index():
    try:
        logger.info("Attempting to render index.html")
        # 检查模板文件是否存在
        template_path = os.path.join(TEMPLATES_DIR, 'index.html')
        if not os.path.exists(template_path):
            logger.error(f"Template file not found: {template_path}")
            return "Template file not found", 500
            
        return render_template('index.html')
    except Exception as e:
        logger.error(f"Error rendering template: {str(e)}", exc_info=True)
        return str(e), 500

@app.route('/generate', methods=['POST'])
def generate_audio():
    if 'audio' not in request.files:
        return '没有上传音频文件', 400
    
    audio_file = request.files['audio']
    text = request.form.get('text', '')
    
    if audio_file.filename == '':
        return '没有选择文件', 400
    
    if text.strip() == '':
        return '文本不能为空', 400
    
    try:
        # 保存上传的音频文件
        filename = secure_filename(audio_file.filename)
        audio_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        audio_file.save(audio_path)
        
        # TODO: 这里需要实现实际的语音克隆和生成逻辑
        # 这可能需要使用额外的AI模型或服务
        # 例如：generated_audio = voice_clone_model.generate(audio_path, text)
        
        # 临时返回上传的音频文件（实际应用中需要替换为生成的新音频）
        return send_file(audio_path, mimetype='audio/wav')
        
    except Exception as e:
        logger.error(f"Error in generate_audio: {str(e)}")
        return str(e), 500

def is_port_in_use(port):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        return s.connect_ex(('localhost', port)) == 0

if __name__ == '__main__':
    # 检查文件是否存在
    css_file = os.path.join(STATIC_DIR, 'css', 'style.css')
    js_file = os.path.join(STATIC_DIR, 'js', 'main.js')
    template_file = os.path.join(TEMPLATES_DIR, 'index.html')
    
    logger.info(f"Checking files:")
    logger.info(f"CSS file exists: {os.path.exists(css_file)}")
    logger.info(f"JS file exists: {os.path.exists(js_file)}")
    logger.info(f"Template file exists: {os.path.exists(template_file)}")
    
    # 如果文件不存在，创建它们
    if not os.path.exists(css_file):
        with open(css_file, 'w', encoding='utf-8') as f:
            f.write('''/* 在这里粘贴之前的 style.css 内容 */''')
    
    if not os.path.exists(js_file):
        with open(js_file, 'w', encoding='utf-8') as f:
            f.write('''/* 在这里粘贴之前的 main.js 内容 */''')
    
    if not os.path.exists(template_file):
        with open(template_file, 'w', encoding='utf-8') as f:
            f.write('''<!-- 在这里粘贴之前的 index.html 内容 -->''')
    
    # 列出所有静态文件
    logger.info("Static files found:")
    for file in glob.glob(os.path.join(STATIC_DIR, '**/*.*'), recursive=True):
        logger.info(f"  {file}")
    
    logger.info(f"Project Root Directory: {ROOT_DIR}")
    logger.info(f"Static Directory: {STATIC_DIR}")
    logger.info(f"Templates Directory: {TEMPLATES_DIR}")
    logger.info(f"Upload Directory: {UPLOAD_DIR}")
    
    # 启动服务器
    port = 5000
    while is_port_in_use(port):
        port += 1
    
    logger.info(f"Starting server on port {port}")
    app.run(debug=True, port=port)