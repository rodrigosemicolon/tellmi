import os

from flask import Flask, request, abort, jsonify, send_from_directory
from flask_sqlalchemy import SQLAlchemy

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:////tmp/test.db'
db = SQLAlchemy(app)
db_session = db.session

class Video(db.Model):
    __tablename__ = "videos"
    id = db.Column(db.Integer, primary_key=True)
    sensorId = db.Column(db.Integer)
    videoDir = db.Column(db.String(120), unique=True)
    language = db.Column(db.String(50))
    age = db.Column(db.String(50))


UPLOAD_DIRECTORY = "project/api_uploaded_files"

if not os.path.exists(UPLOAD_DIRECTORY):
    os.makedirs(UPLOAD_DIRECTORY)


@app.teardown_appcontext
def shutdown_session(exception=None):
    db_session.remove()

@app.route("/video", methods=["POST"])
def define_video():
    print(request.form)
    new_vid = Video(sensorId=request.form["sensor"], videoDir=request.form["video"], language=request.form["language"], age=request.form["age"])
    db_session.add(new_vid)
    db_session.commit()
    return "nothing"

@app.route("/video/<int:sensor>/<string:lang>/<string:age>")
def get_closest_video(sensor, lang, age):
    print("here")
    vid = Video.query.filter_by(sensorId=sensor, language=lang, age=age).first_or_404()
    print(vid)
    return send_from_directory(UPLOAD_DIRECTORY, vid.videoDir, as_attachment=True)


@app.route("/files")
def list_files():
    """Endpoint to list files on the server."""
    files = []
    for filename in os.listdir(UPLOAD_DIRECTORY):
        path = os.path.join(UPLOAD_DIRECTORY, filename)
        if os.path.isfile(path):
            files.append(filename)
    return jsonify(files)


@app.route("/files/<path:path>")
def get_file(path):
    """Download a file."""
    return send_from_directory(UPLOAD_DIRECTORY, path, as_attachment=True)


@app.route("/files/<filename>", methods=["POST"])
def post_file(filename):
    """Upload a file."""

    if "/" in filename:
        # Return 400 BAD REQUEST
        abort(400, "no subdirectories allowed")

    with open(os.path.join(UPLOAD_DIRECTORY, filename), "wb") as fp:
        fp.write(request.data)

    # Return 201 CREATED
    return "", 201


if __name__ == "__main__":
    db.create_all()
    app.run(debug=True,host="0.0.0.0", port=5000)